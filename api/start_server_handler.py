import boto3
import json
import os
import traceback

REGION = 'us-west-1'
CLUSTER = 'slardcraft'
SERVICE = 'slardcraft-server'

def lambda_handler(event, context):
    try:
        """Updates the desired count for a service."""
        print(event)
        # body = json.loads(event.get('body'))
        code = event.get('code')
        if not code:
            msg = {
                "status": "MISSING_CODE",
                "message": "Code is missing from message body."
            }
            print(f"message: {msg}")
            return msg
            
        if code != os.getenv('CODE'):
            msg = {
                "status": "WRONG_CODE",
                "message": "Provided code is incorrect."
            }
            print(f"message: {msg}")
            return msg
            
        ecs = boto3.client('ecs', region_name=REGION)
        response = ecs.describe_services(
            cluster=CLUSTER,
            services=[SERVICE],
        )
    
        desired = response["services"][0]["desiredCount"]
    
        if desired == 0:
            ecs.update_service(
                cluster=CLUSTER,
                service=SERVICE,
                desiredCount=1,
            )
            print("Updated desiredCount to 1")
            msg = {
                "status": "STARTING_SERVER",
                "message": "Provided code is correct. Starting server."
            }
            return msg
        else:
            print("desiredCount already at 1")
            msg = {
                "status": "SERVER_ALREADY_STARTED",
                "message": "Provided code is correct. Server already started."
            }
            return msg
    except Exception as e:
        traceback.print_exc()
        return {
            "status": "ERROR",
            "message": "Error occured while processing code."
        }
        

def message(statusCode, body):
    return {
            'statusCode': statusCode,
            "isBase64Encoded": False,
            'headers': {
                "Access-Control-Allow-Origin" : "*",
                'Access-Control-Allow-Headers': 'Content-Type, Authorization, Content-Length, X-Requested-With, Accept',
                'Access-Control-Allow-Methods': 'GET,PUT,POST,DELETE,OPTIONS'
            },
            'body': json.dumps(body)
        }