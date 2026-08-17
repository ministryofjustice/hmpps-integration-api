import boto3
import json
import sys
import time

sqs = boto3.client("sqs", region_name="eu-west-2")

def send_sqs_messages(queue_url):
    """
    Send messages to the specified SQS queue. Reads each line from stdin,
    parses it as a single JSON message, and sends it to the queue.

    Example usage for sending messages from a file:

     NAMESPACE=... SECRET_NAME=... source aws-creds-from-k8s.sh && \
     cat sqs-messages.log | python3 sqs-utils.py send "$SQS_QUEUE_URL" | tee send-output.log

    :param queue_url: The URL of the SQS queue.
    :return:
    """
    success = 0
    failure = 0
    for line in sys.stdin:
        # Sleep for 200 ms
        time.sleep(200 / 1000)
        message = json.loads(line)
        message_attributes = dict((k, {"StringValue": v["Value"], "DataType": "String"})
                                  for k, v in message["MessageAttributes"].items())
        response = sqs.send_message(MessageBody=json.dumps(message),
                                    MessageAttributes=message_attributes,
                                    QueueUrl=queue_url)
        print(response)
        if response["ResponseMetadata"]["HTTPStatusCode"] == 200:
            success += 1
        else:
            failure += 1

    print(f"Successfully sent {success} messages. Failures={failure}")

if __name__ == '__main__':
    if len(sys.argv) != 3:
        print(
            f"Usage: {sys.argv[0]} <count|read|send> <sqs_queue_url>", file=sys.stderr)
        sys.exit(1)

    if sys.argv[1] == "send":
        send_sqs_messages(sys.argv[2])
