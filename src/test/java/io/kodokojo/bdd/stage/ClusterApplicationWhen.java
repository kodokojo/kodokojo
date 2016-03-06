package io.kodokojo.bdd.stage;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.bdd.MarathonIsPresent;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.commons.utils.servicelocator.marathon.MarathonServiceLocator;
import io.kodokojo.model.*;
import io.kodokojo.project.dns.DnsEntry;
import io.kodokojo.project.dns.DnsManager;
import io.kodokojo.project.dns.route53.Route53DnsManager;
import io.kodokojo.project.starter.brick.marathon.MarathonBrickManager;
import io.kodokojo.service.BrickFactory;
import io.kodokojo.service.DefaultBrickFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class ClusterApplicationWhen <SELF extends ClusterApplicationWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    MarathonIsPresent marathon;

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

        //brickConfigurations.add(new BrickConfiguration(Brick.DOCKER_REGISTRY));
        //brickConfigurations.add(new BrickConfiguration(Brick.HAPROXY));

        loadBalancerIp = "52.50.95.225";    //Ha proxy may be reloadable in a short future.

        StackConfiguration stackConfiguration = new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, loadBalancerIp,40022);
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
        MarathonBrickManager marathonBrickManager = new MarathonBrickManager(marathon.getMarathonUrl(), new MarathonServiceLocator(marathon.getMarathonUrl()));

        Future<Void> scmFut = startAndConfigure(marathonBrickManager, projectConfiguration, BrickType.SCM);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Future<Void> ciFut = startAndConfigure(marathonBrickManager, projectConfiguration, BrickType.CI);
        try {
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

}
