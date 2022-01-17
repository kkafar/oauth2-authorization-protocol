import sys
import config as cfg
from flask import Flask


app = Flask(__name__)

@app.route("/")
def echo():
    return "<p>Everybody hands up! We are going crazy!</p>"



if __name__ == "__main__":
    match sys.argv[1]:
        case "debug":
            app.run(port=cfg.config['port'])
        case "dev":
            app.run(port=cfg.config['port'], host=cfg.config['hostAddress'])
        case _:
            app.run(port=cfg.config['port'])
            