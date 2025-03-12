exports.handler = async (event) => {
    const { resource, path, httpMethod, headers, queryStringParameters, job } = event;
    console.log("\n\n Reserve seats now...");
    console.log(`resource: ${resource}`);
    console.log(`path: ${path}`);
    console.log(`httpMethod: ${httpMethod}`);
    console.log(`headers: ${headers}`);
    console.log(`queryStringParameters: ${queryStringParameters}`);
    console.log(`job: ${job}`);

    if ("seats" !== job.variables.simulateBookingFailure) {
        console.log("Successul :-)");
        return {
            body: JSON.stringify({ reservationId: "1234" }, null, 2),
            statusCode: 200,
        };
      } else {
        console.log("ERROR: Seats could not be reserved!");
        return {
            body: "ErrorSeatsNotAvailable",
            statusCode: 500,
        };
      }
  };
