import json
import boto3
from mcstatus import MinecraftServer

client = boto3.client('ecs')

def lambda_handler(event, context):
    try:
        response = client.describe_services(
            cluster='arn:aws:ecs:us-west-1:520752086628:cluster/slardcraft',
            services=[
                'slardcraft-server'
            ]
        )
        numDesiredCount = response.get('services')[0].get('desiredCount')
        numRunningCount = response.get('services')[0].get('runningCount')
        
        if numRunningCount > 0:
            # query minecraft server with dinner bones api
            status = None
            try:
                server = MinecraftServer.lookup("play.bordsmp.jacobyng.com:25565")
                status = server.status()
            except Exception as e:
                print(repr(e))
            if status:
                # return player everything.
                player_list = []
                if status.players.sample:
                    player_list = status.players.sample
                
                return message(200, {
                        'serverStatus': 'MINECRAFT_ONLINE',
                        'max': status.players.max,
                        'online': status.players.online,
                        'players': [
                            {
                                'id': player.id,
                                'name': player.name
                            }
                            for player in player_list
                        ]
                    })
            else:
                return message(200, {
                        'serverStatus': 'TASK_ONLINE',
                        'message': 'Task is up, but minecraft server has yet to initalize.'
                    })
        if numDesiredCount > 0:
            return message(200, {
                    'serverStatus': 'TASK_PENDING',
                    'message': 'Task is still pending compute resources.'
                })
        return message(200, {
                'serverStatus': 'TASK_OFFLINE',
                'message': 'Task is completely offline, no task pending.'
            })
 
    except Exception as e:
        print(repr(e))
        
        return message(500, {
                'serverStatus': 'UNKNOWN',
                'message': repr(e)
            })



def message(statusCode, body):
    return {
            'statusCode': statusCode,
            "isBase64Encoded": False,
            'headers': {
                "Access-Control-Allow-Origin" : "*",
            },
            'body': json.dumps(body)
        }