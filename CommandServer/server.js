const express = require('express');
const bodyParser = require('body-parser');
const {exec} = require('child_process');
const os = require('os');

const app = express();
app.use(bodyParser.urlencoded({extended: true})); //or app.use(express.urlencoded())
app.use(bodyParser.json()) //or app.use(express.json())

var ipAddress = os.networkInterfaces()['Wi-Fi 2'][3].address;

app.get("/cmd", (req, res) => {
    res.send("Correct route");
});

app.post("/cmd", (req, res) => {
    console.log(req.body);  
    var data = req.body;
    console.log(data.cmd);
    if (data.pin === 1234) {
        exec(data.cmd, (error, stdout, stderr) => {
            if (error) {
                console.log(`error: ${error.message}`);
                res.json({response: `error: ${error.message}`});
                return;
            }
            if (stderr) {
                console.log(`stderr: ${stderr}`);
                res.json({response: `stderr: ${stderr}`});
                return;
            }
            console.log(`stdout: ${stdout}`);
            res.json({response: `stdout: ${stdout}`});
        });
    } else {
        console.log("Unauthorized access to server.");
        res.json({response: "Unauthorized access to server."});
    }
});

app.listen(3000, ipAddress, () => {
    console.log("Server started at port 3000 with IP " + ipAddress);
});