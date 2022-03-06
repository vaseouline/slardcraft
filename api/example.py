from mcstatus import MinecraftServer
import json

# You can pass the same address you'd enter into the address field in minecraft into the 'lookup' function
# If you know the host and port, you may skip this and use MinecraftServer("example.org", 1234)
server = MinecraftServer.lookup("localhost")

# 'status' is supported by all Minecraft servers that are version 1.7 or higher.
status = server.status()
print(f"The server has {status.players.online} players and replied in {status.latency} ms")

print(status.players.sample[0].name)

print(json.dumps({
    'players': [
        {
            'id': player.id,
            'name': player.name
        }
        for player in status.players.sample
    ]
}
)
)