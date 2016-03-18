package io.kodokojo.bdd.stage;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.squareup.okhttp.*;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.bdd.MarathonIsPresent;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.commons.utils.servicelocator.marathon.MarathonServiceLocator;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.*;
import io.kodokojo.service.dns.DnsEntry;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.dns.route53.Route53DnsManager;
import io.kodokojo.project.starter.marathon.MarathonBrickManager;
import io.kodokojo.service.BrickFactory;
import io.kodokojo.service.DefaultBrickFactory;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;


public class ClusterApplicationWhen<SELF extends ClusterApplicationWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    String marathonUrl;

    @ExpectedScenarioState
    String domain;

    @ExpectedScenarioState
    User currentUser;

    @ProvidedScenarioState
    ProjectConfiguration projectConfiguration;

    @ProvidedScenarioState
    String loadBalancerIp;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public SELF i_create_a_default_project(String projectName) {

        BrickFactory brickFactory = new DefaultBrickFactory(null);
        Set<StackConfiguration> stackConfigurations = new HashSet<>();
        Set<BrickConfiguration> brickConfigurations = new HashSet<>();

        brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.JENKINS)));
        brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.GITLAB)));
        brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.HAPROXY),false));

        loadBalancerIp = "52.50.157.189";    //Ha proxy may be reloadable in a short future.

        String url = marathonUrl + "/v2/artifacts/config/acme.json";

        OkHttpClient httpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", "acme.json",
                        RequestBody.create(MediaType.parse("application/json"), ("{\n" +
                                "  \"projectName\": \"acme\",\n" +
                                "  \"sshPort\": 42022\n" +
                                "}").getBytes()))
                .build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        try {
            Response response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            KeyPair keyPair = RSAUtils.generateRsaKeyPair();
            SSLKeyPair caSSL = SSLUtils.createSelfSignedSSLKeyPair("Fake root", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
            SSLKeyPair sslKeyPair = SSLUtils.createSSLKeyPair("acme.kodokojo.io", caSSL.getPrivateKey(), caSSL.getPublicKey(), caSSL.getCertificates(), TimeUnit.DAYS.toMillis(3 * 31), true);
            SSLKeyPair ciSslKeyPaire = SSLUtils.createSSLKeyPair("ci.acme.kodokojo.io", sslKeyPair.getPrivateKey(), sslKeyPair.getPublicKey(), sslKeyPair.getCertificates());
            SSLKeyPair scmSslKeyPaire = SSLUtils.createSSLKeyPair("scm.acme.kodokojo.io", sslKeyPair.getPrivateKey(), sslKeyPair.getPublicKey(), sslKeyPair.getCertificates());
            Writer writer = new StringWriter();
            SSLUtils.writeSSLKeyPairPem(ciSslKeyPaire, writer);
            pushCertificate("acme", "ci", writer.toString().getBytes());
            writer = new StringWriter();
            SSLUtils.writeSSLKeyPairPem(scmSslKeyPaire, writer);
            pushCertificate("acme", "scm", writer.toString().getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StackConfiguration stackConfiguration = new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, loadBalancerIp, 40022);
        stackConfigurations.add(stackConfiguration);
        if (false) {
            DnsManager dnsManager = new Route53DnsManager("kodokojo.io", Region.getRegion(Regions.EU_WEST_1));
            dnsManager.createDnsEntry(new DnsEntry("scm.acme.kodokojo.io", DnsEntry.Type.A, loadBalancerIp));
            dnsManager.createDnsEntry(new DnsEntry("ci.acme.kodokojo.io", DnsEntry.Type.A, loadBalancerIp));
            dnsManager.createDnsEntry(new DnsEntry("repo.acme.kodokojo.io", DnsEntry.Type.A, loadBalancerIp));
        }
        this.projectConfiguration = new ProjectConfiguration(projectName, currentUser.getEmail(), stackConfigurations, Collections.singletonList(currentUser));

        return self();
    }

    public SELF i_start_the_project() {
        MarathonBrickManager marathonBrickManager = new MarathonBrickManager(marathonUrl, new MarathonServiceLocator(marathonUrl));

        Future<Void> lbFut = startAndConfigure(marathonBrickManager, projectConfiguration, BrickType.LOADBALANCER);
        Future<Void> scmFut = startAndConfigure(marathonBrickManager, projectConfiguration, BrickType.SCM);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Future<Void> ciFut = startAndConfigure(marathonBrickManager, projectConfiguration, BrickType.CI);
        try {
            lbFut.get();
            ciFut.get();
            scmFut.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return self();
    }

    private Future<Void> startAndConfigure(MarathonBrickManager marathonBrickManager, ProjectConfiguration projectConfiguration, BrickType brickType) {
        return executorService.submit((Callable<Void>) () -> {
            Set<Service> ciServices = marathonBrickManager.start(projectConfiguration, brickType);
            System.out.println(brickType + " : " + StringUtils.join(ciServices, ","));
            marathonBrickManager.configure(projectConfiguration, brickType);

            return null;
        });
    }

    private void pushCertificate(String project, String entityType, byte[] certificat) {

        String url = marathonUrl + "/v2/artifacts/ssl/" + project + "/" + entityType + "/" + project + "-" + entityType + "-server.pem";
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file",  project + "-" + entityType + "-server.pem",
                        RequestBody.create(MediaType.parse("application/text"), certificat))
                .build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        try {
            Response response = httpClient.newCall(request).execute();
            System.out.println("Send certificate to " + url);
            System.out.println("Certificate :\n" + new String(certificat));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
