package aws.ec2.webapp;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import java.util.List;


public class App {
    public static void main(String[] args) {
        String bucket_name = "njit-cs-643";
        String queue_name = "queue.fifo";
        try (S3Client s3cli = S3Client.builder().region(Region.US_EAST_1).build();
             SqsClient sqscli = SqsClient.builder().region(Region.US_EAST_1).build();
             RekognitionClient rekogClient = RekognitionClient.builder().region(Region.US_EAST_1).build()) {

            GetQueueUrlResponse get_QueueUrl_Response =
                    sqscli.getQueueUrl(GetQueueUrlRequest.builder().queueName(queue_name).build());
            String queueUrl = get_QueueUrl_Response.queueUrl();
            System.out.println("Url of the sqs queue is -> " + queueUrl);

            ListObjectsV2Request v2_request = ListObjectsV2Request.builder().bucket(bucket_name).build();
            ListObjectsV2Response res;
            do {
                res = s3cli.listObjectsV2(v2_request);
                for (S3Object object : res.contents()) {
                    String photo = object.key();
                    Image image = Image.builder()
                            .s3Object(s -> s.bucket(bucket_name).name(photo))
                            .build();

                    DetectLabelsRequest reqst = DetectLabelsRequest.builder()
                            .image(image)
                            .maxLabels(9)
                            .minConfidence(75F)
                            .build();

                    DetectLabelsResponse result = rekogClient.detectLabels(reqst);
                    List<Label> labels = result.labels();

                    for (Label lab : labels) {
                        if (lab.name().equals("Car") && lab.confidence() > 90) {
                            System.out.print("Detected Car(label) in:  " + photo + " => ");
                            System.out.print("label: " + lab.name() + " ,");
                            System.out.println("Confidence: " + lab.confidence().toString());

                            String messageBody = String.format("'Car' in image '%s' is Recognised with confidence %.2f%%", photo, lab.confidence());

                            System.out.println("Message pushed to SQS Queue: " + messageBody);
                            sqscli.sendMessage(SendMessageRequest.builder()
                                    .queueUrl(queueUrl)
                                    .messageBody(messageBody)
                                    .messageGroupId("Object_Image_Recog")
                                    .build());
                        }
                    }
                }
                v2_request = v2_request.toBuilder().continuationToken(res.nextContinuationToken()).build();
            } while (res.isTruncated());
            sqscli.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody("-1")
                    .messageGroupId("Object_Image_Recog")
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
