package io.kodokojo.commons.utils;

import com.github.dockerjava.api.command.CreateContainerResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isBlank;

public class HttpServiceChecker implements DockerTestApplicationBuilder.ServiceChecker {

    private final int exposedPort;

    private final String location;

    private final int timeout;

    public HttpServiceChecker(int exposedPort, int timeout, String location) {
        if (isBlank(location)) {
            throw new IllegalArgumentException("location must be defined.");
        }
        this.exposedPort = exposedPort;
        this.timeout = timeout;
        this.location = location;
    }

    public HttpServiceChecker(int exposedPort) {
        this(exposedPort, 10000, "/");
    }

    @Override
    public void checkServiceIsRunning(DockerTestSupport dockerTestSupport, CreateContainerResponse createContainerResponse) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().get().url("http://" + dockerTestSupport.getServerIp() + ":" + exposedPort + location).build();
        Response response = null;
        boolean ready = false;

        long timeout = System.currentTimeMillis() + this.timeout;
        do {
            try {
                response = client.newCall(request).execute();
                ready = response.code() >= 200 && response.code() < 400;
            } catch (IOException e) {
                //  Waiting ...
            } finally {
                if (response != null) {
                    IOUtils.closeQuietly(response.body());
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } while (!ready && System.currentTimeMillis() < timeout && !Thread.currentThread().isInterrupted());
    }
}
