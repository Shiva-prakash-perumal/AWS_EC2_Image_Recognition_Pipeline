package aws.ec2.webapp;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import java.util.List;

public class App {
    public static void main(String[] args) {
        Region client_region = Region.US_EAST_1;
        String bucket_name = "njit-cs-643";
        String queueName = "queue.fifo";

        try (S3Client s3cli = S3Client.builder().region(client_region).build();
             RekognitionClient rekogClient = RekognitionClient.builder().region(client_region).build();
             SqsClient sqs_client = SqsClient.builder().region(client_region).build()) {

            String queue_url = sqs_client.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl();
            System.out.println("Url of the sqs queue is -> " + queue_url);

            ListObjectsV2Response res = s3cli.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket_name).build());

            List<Message> message = receiveMessages(sqs_client, queue_url);
            System.out.println("Size of message is "+message.size());
            for (S3Object object : res.contents()) {
                if (!message.isEmpty()) {
                    for (Message messages : message) {
                       if (messages.body().contains(object.key())) {
                            DetectTextRequest req = DetectTextRequest.builder()
                                    .image(Image.builder()
                                            .s3Object(software.amazon.awssdk.services.rekognition.model.S3Object.builder()
                                                    .name(object.key())
                                                    .bucket(bucket_name)
                                                    .build())
                                            .build())
                                    .build();
                            System.out.println("Received Image---->> "+object.key());
                           rekogClient.detectText(req).textDetections().forEach(textDetection -> {
                                System.out.println("text Detected from car is : " + textDetection.detectedText() + ", which has Confidence: " + textDetection.confidence());
                            });
                            System.out.println("****************************************************");
                       }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Message> receiveMessages(SqsClient sqsClient, String queueUrl) {
        System.out.println("\nReceived messages");
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .build();
            return sqsClient.receiveMessage(receiveMessageRequest).messages();
        } catch (Exception e) {
            System.err.println("Error in receiving messages: " + e.getMessage());
            return null;
        }
    }
}
