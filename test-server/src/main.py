import sys
import config as cfg
from flask import Flask


app = Flask(__name__)

@app.route("/")
def echo():
    return "<p>Everybody hands up! We are going crazy!</p>"



if __name__ == "__main__":
    assert len(sys.argv) == 2, "Server mode name must be provided! 'debug' or 'dev'"
    assert sys.argv[1] in {'debug', 'dev'}, "Server mode name must be provided! 'debug' or 'dev'"

    match sys.argv[1]:
        case "debug":
            configuration = {'port': cfg.config['port']}
        case "dev":
            configuration = {'port': cfg.config['port'], 'host': cfg.config['hostAddress']}
            
    app.run(**configuration)
