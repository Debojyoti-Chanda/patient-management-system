package com.pm.billing_service.grpc;

import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.stub.StreamObserver; // Imports StreamObserver, a core gRPC interface. This is used by both clients and servers to handle streams of messages. For a unary (single request, single response)
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//This annotation is provided by the net.devh Spring Boot gRPC starter. It tells Spring Boot that this class is a gRPC service implementation
// and should be automatically registered with the gRPC server and managed
//by Spring's application context. It effectively makes this class a Spring bean.
@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(billing.BillingRequest billingRequest, StreamObserver<billing.BillingResponse> responseObserver){
        log.info("Create BillingAccount request received {}",billingRequest.toString());

        //business logic - eg  save to database, perform calculations etc
        // StreamObserver<billing.BillingResponse> responseObserver: This is the mechanism to send the response(s) back to
        // the client. For a unary RPC (like this one), you'll call onNext() once with the response and then onCompleted()
        // to signal the end of the call. For streaming RPCs, you'd call onNext() multiple times.

        BillingResponse billingResponse = BillingResponse.newBuilder().setAccountId("12345").setStatus("ACTIVE").build();
        // This line constructs a BillingResponse object. BillingResponse is a generated Protobuf message class.
        //Protobuf generated classes use a builder pattern (newBuilder(), setX(), build()) for creating immutable message
        // instances, which is a common and efficient way to construct complex objects.
        responseObserver.onNext(billingResponse);
        // Sends the constructed billingResponse back to the client. For a unary RPC, this is the single response .
        responseObserver.onCompleted();
        // Signals to the client that the server has finished processing the RPC and no more messages will be sent for
        // this particular call. This is crucial for the client to know when the response stream is complete.
    }
}
