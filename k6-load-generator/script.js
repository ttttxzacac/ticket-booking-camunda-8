import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  // A number specifying the number of VUs to run concurrently.
  vus: 1000,
  // A string specifying the total duration of the test run.
  duration: '30s',
  //thresholds: {
  //http_req_failed: ['rate<0.01'], // http errors should be less than 1%
  //  http_req_duration: ['p(95)<200'], // 95% of requests should be below 200ms
  //},
};

// Use the following options to ramp the number of VUs up and down during the test
export const optionsForRampingVus = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '1m30s', target: 10 },
    { duration: '45s', target: 30 },
    { duration: '20s', target: 0 },
  ],
};

var round_robin_next_task = 1;
const task1IpAddress = "16.170.204.83";
const task2IpAddress = "13.53.125.144";

// The default exported function is gonna be picked up by k6 as the entry point for the test script. It will be executed repeatedly in "iterations" for the whole duration of the test.
export default function () {
  const payload = JSON.stringify({
    simulateBookingFailure: false
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  var nextTaskIp;
  if (round_robin_next_task == 1) {
    round_robin_next_task = 2;
    nextTaskIp = task1IpAddress;
  } else {
    round_robin_next_task = 1;
    nextTaskIp = task2IpAddress;
  }
  // Change the IpAddress with the ECS domain name/ip address
  let response = http.put(`http://${nextTaskIp}:8080/ticket`, payload, params);

  // Sleep for 1 second to simulate real-world usage
  //sleep(1);

  check(response, {
    'is status 200': (r) => r.status === 200,
    'response time is less than 200ms': (r) => r.timings.duration < 200,
  });
}

/*
 Run k6 with the following command:
k6 run script.js

Run k6 with the specified vus and duration:
k6 run --vus 10 --duration 30s script.js

Run following command to enable web dashboard:
K6_WEB_DASHBOARD=true k6 run script.js

You can reach the dasboard on:
http://127.0.0.1:5665
 */
