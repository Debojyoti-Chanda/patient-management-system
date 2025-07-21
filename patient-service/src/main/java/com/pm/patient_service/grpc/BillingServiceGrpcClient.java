package com.pm.patient_service.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc; // 4. Import generated gRPC service class (contains stubs)
import io.grpc.ManagedChannel;  // 5. gRPC core: manages connection to server
import io.grpc.ManagedChannelBuilder; // 6. gRPC core: builds a ManagedChannel
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    //synchronous client call to the grpc server running in the billing-service container
    // The client application has a generated gRPC client stub (e.g., BillingServiceGrpc.BillingServiceBlockingStub or
    // BillingServiceGrpc.BillingServiceStub for asynchronous calls).
    // gRPC also generates asynchronous stubs (BillingServiceStub) for non-blocking operations, which use StreamObserver patterns.
    final private BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    // localhost:9001/BillingService/CreatePatientAccount
    //
    // 14. Constructor for dependency injection and gRPC channel/stub setup
    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int port
    ){
        log.info("Connecting to Billing Service at {}:{}",serverAddress,port);

        // ManagedChannelBuilder.forAddress(serverAddress,port): Creates a builder for a ManagedChannel, specifying the target host and port of the gRPC server.
        //.usePlaintext(): Crucial for development environments. This disables TLS (Transport Layer Security) encryption
        // for the gRPC connection. In production, you would never use usePlaintext() and instead configure TLS certificates.
        // Without usePlaintext(), gRPC defaults to requiring TLS, and if your server isn't set up for TLS, connections will fail.
        //.build(): Constructs the ManagedChannel instance. This channel is then used to create the client stub. A single
        // ManagedChannel can be used for multiple gRPC calls and multiple stubs.
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress,port).usePlaintext().build();

        //his static factory method (from the generated BillingServiceGrpc class) takes the ManagedChannel and returns
        // a new instance of the BillingServiceBlockingStub. This stub is now ready to make gRPC calls.
        blockingStub = BillingServiceGrpc.newBlockingStub(channel);

    }
    public BillingResponse createBillingAccount(String patientId,String name, String email){

        BillingRequest request = BillingRequest.newBuilder().setPatientId(patientId).setName(name).setEmail(email).build();
        // The client calls a method on this stub, createBillingAccount from the billing_service method
        // The stub takes the client's BillingRequest object (which is a Protobuf message).
        BillingResponse response = blockingStub.createBillingAccount(request);
        log.info("Received response from billing service via gRPC: {}",response);
        return response;
    }
}
