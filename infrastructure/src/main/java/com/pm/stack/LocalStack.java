package com.pm.stack;


import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.CloudMapNamespaceOptions;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.stream.Collectors;

//  A Stack represents a single unit of deployment in AWS CloudFormation. Everything you define within this class will
//  become part of a single CloudFormation stack.
public class LocalStack extends Stack   {
    private final Vpc vpc;
    private final Cluster ecsCluster;

    // final App scope: This is the parent "scope" for the stack. An App is the top-level container for one or more stacks.
    // final String id: This is a unique identifier for the stack within its scope. AWS CDK uses this ID to generate a
    // logical ID for the CloudFormation stack.
    // final StackProps props: This is an object that contains properties for configuring the stack, such as the region,
    // account, or other stack-specific settings.
    public LocalStack(final App scope, final String id, final StackProps props){
        super(scope,id,props);
        this.vpc = createVpc();
        DatabaseInstance authServiceDb = createDatebase("AuthServiceDB","auth-service-db");
        DatabaseInstance patientServiceDb = createDatebase("PatientServiceDB","patient-service-db");

        CfnHealthCheck authDbHealthCheck = createDbHealthCheck(authServiceDb,"AuthServiceDBHealthCheck");
        CfnHealthCheck patientDbHealthCheck = createDbHealthCheck(patientServiceDb,"PatientDBHealthCheck");

        CfnCluster mskCluster = createMskCluster();

        this.ecsCluster = createEcsCluster();
    }
    private Vpc createVpc(){
        return Vpc.Builder
                .create(this,"PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance createDatebase(String id, String dbName){
        return DatabaseInstance.Builder
                .create(this,id)
                .engine(DatabaseInstanceEngine
                        .postgres(PostgresInstanceEngineProps
                                .builder()
                                .version(PostgresEngineVersion.VER_17_5)
                                .build())
                ).vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName) 
                .removalPolicy(RemovalPolicy.DESTROY )
                .build();
    }

    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db,String id){
        return CfnHealthCheck.Builder.create(this,id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
                .build();
    }

    private CfnCluster createMskCluster(){
        return CfnCluster.Builder
                .create(this,"MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.8.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.xlarge")
                        .clientSubnets(vpc.getPrivateSubnets().stream()
                                .map(ISubnet::getSubnetId)
                                .collect(Collectors.toList())
                        )
                        .brokerAzDistribution("DEFAULT")
                        .build()
                ).build() ;
    }

    private Cluster createEcsCluster(){
        return Cluster.Builder.create(this,"PatientManagementCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        .name("patient-management.local")
                        .build()
                ).build();
    }

    public static void main(String[] args) {
        //App app = new App(...): This creates a new instance of an App, which is the root of your AWS CDK application.
        App app = new  App(AppProps.builder().outdir("./cdk.out").build());
        // StackProps props = StackProps.builder()...: This creates an object to configure the stack.

        StackProps props = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();
        new LocalStack(app,"localstack",props);
        // The synth() method is where the magic happens. It takes the programmatic definition of your infrastructure
        // (the LocalStack and any resources you've defined within it) and "synthesizes" it into a CloudFormation
        // template. This template is what AWS uses to create, update, or delete your resources. The synthesized template
        // is saved in the cdk.out directory.
        app.synth();
        System.out.println("App synthesizing in progress....");
    }
}
