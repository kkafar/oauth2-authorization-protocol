import sys
import config as cfg
from flask import Flask, request


app = Flask(__name__)


@app.route("/")
def echo():
    return "<p>Response from server</p>"


@app.route("/auth")
def authorization_form():
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>Login</title>
        </head>
        <body>
        <form action="$SUBMIT_URL" method="post" enctype="application/x-www-form-urlencoded">
            <label for="login">Login:</label>
            <input type="text" id="login" name="login"><br><br>
            <label for="password">Password:</label>
            <input type="password" id="password" name="password"><br><br>
            <input type="submit" value="Submit">
        </form>
        </body>
        </html>"""


@app.route("/data", methods=['POST', 'GET'])
def reply_with_json():
    return {'statusCode': 200, 'content': 'some content'}


if __name__ == "__main__":
    assert len(sys.argv) == 2, "Server mode name must be provided! 'debug' or 'dev'"
    assert sys.argv[1] in {'debug', 'dev'}, "Server mode name must be provided! 'debug' or 'dev'"

    match sys.argv[1]:
        case "debug":
            configuration = {'port': cfg.config['port']}
        case "dev":
            configuration = {'port': cfg.config['port'], 'host': cfg.config['hostAddress']}
            
    app.run(**configuration)
