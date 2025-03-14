package dev.httpmarco.polocloud.suite.cluster.grpc;

import dev.httpmarco.polocloud.grpc.ClusterService;
import dev.httpmarco.polocloud.grpc.ClusterSuiteServiceGrpc;
import dev.httpmarco.polocloud.suite.PolocloudSuite;
import dev.httpmarco.polocloud.suite.cluster.configuration.ClusterGlobalConfig;
import dev.httpmarco.polocloud.suite.cluster.global.ClusterSuiteData;
import dev.httpmarco.polocloud.suite.cluster.global.GlobalCluster;
import dev.httpmarco.polocloud.suite.cluster.global.suites.ExternalSuite;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ClusterSuiteGrpcHandler extends ClusterSuiteServiceGrpc.ClusterSuiteServiceImplBase {

    private static final Logger log = LogManager.getLogger(ClusterSuiteGrpcHandler.class);

    @Override
    public void attachSuite(ClusterService.ClusterSuiteAttachRequest request, StreamObserver<ClusterService.ClusterSuiteAttachResponse> responseObserver) {
        var response = ClusterService.ClusterSuiteAttachResponse.newBuilder().setSuccess(false);
        var localConfig = PolocloudSuite.instance().config().cluster();

        //todo
        if (localConfig instanceof ClusterGlobalConfig globalConfig) {
            if (!globalConfig.privateKey().equals(request.getSuitePrivateKey())) {
                response.setMessage("Invalid private key!");
            } else {
                if (globalConfig.id().equals(request.getSuiteId())) {
                    response.setSuccess(true);
                    response.setToken(globalConfig.token());
                } else {
                    response.setMessage("Invalid token!");
                }
            }

        } else {
            response.setMessage("Cluster is not configured correctly! The binded suite is local");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void requestState(ClusterService.EmptyCall request, StreamObserver<ClusterService.ClusterSuiteStateResponse> responseObserver) {
        var response = ClusterService.ClusterSuiteStateResponse.newBuilder();

        //todo
        if (PolocloudSuite.instance().cluster() instanceof GlobalCluster globalCluster) {
            response.setState(globalCluster.state());
        } else {
            // this should never happen, but if we are here, the cluster is not configured correctly
            log.warn("Cluster is not configured correctly! The bound suite is local, but we need a global cluster!");
            response.setState(ClusterService.State.INVALID);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void drainCluster(ClusterService.SuiteDrainRequest request, StreamObserver<ClusterService.EmptyCall> responseObserver) {
        //todo
        if (PolocloudSuite.instance().cluster() instanceof GlobalCluster globalCluster) {
            var externalSuite = globalCluster.find(request.getId());
            if (externalSuite == null) {
                log.error("The suite {} could not be found in the cluster! But external tries to drain it!", request.getId());
            } else {
                externalSuite.close();
                globalCluster.suites().remove(externalSuite);
                log.info("The suite {} has been drained from the cluster!", externalSuite.id());
            }
        } else {
            log.warn("Cluster is not configured correctly! The bound suite is local, but we need a global cluster!");
        }


        responseObserver.onNext(ClusterService.EmptyCall.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void runtimeHandshake(ClusterService.SuiteRuntimeHandShakeRequest request, StreamObserver<ClusterService.EmptyCall> responseObserver) {
        var newSuite = new ExternalSuite(new ClusterSuiteData(request.getId(), request.getHostname(), request.getPrivateKey(), request.getPort()));

        if (PolocloudSuite.instance().cluster() instanceof GlobalCluster globalCluster) {
            if (globalCluster.find(request.getId()) != null) {
                log.error("The suite {} is already registered in the cluster! Maybe a hidden data?", request.getId());
            } else {
                // welcome new suite
                globalCluster.suites().add(newSuite);
                newSuite.state(newSuite.available() ? newSuite.clusterStub().requestState(ClusterService.EmptyCall.newBuilder().build()).getState() : ClusterService.State.OFFLINE);

                if (newSuite.state() == ClusterService.State.AVAILABLE) {
                    log.info("The suite {} is now online and bound to the cluster!", newSuite.id());
                }
                // the suite status task will handle the rest
            }

        }

        responseObserver.onNext(ClusterService.EmptyCall.newBuilder().build());
        responseObserver.onCompleted();
    }
}
