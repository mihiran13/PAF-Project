const http = require('http');

const data = JSON.stringify({ email: "it23864856@my.sliit.lk", password: "yositha1234" });

const options = {
  hostname: 'localhost',
  port: 8080,
  path: '/api/auth/login',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': data.length
  }
};

const req = http.request(options, res => {
  let body = '';
  res.on('data', d => body += d);
  res.on('end', () => {
    const loginRes = JSON.parse(body);
    const token = loginRes.data.accessToken;
    
    const req2 = http.request({
      hostname: 'localhost',
      port: 8080,
      path: '/api/tickets?size=10',
      method: 'GET',
      headers: { 'Authorization': 'Bearer ' + token }
    }, res2 => {
      let body2 = '';
      res2.on('data', d => body2 += d);
      res2.on('end', () => {
        console.log("Tickets Response:", body2);
      });
    });
    req2.end();
  });
});

req.write(data);
req.end();
