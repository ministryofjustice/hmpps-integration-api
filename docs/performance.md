# Performance testing 11/12/2023

Environment: Development

The endpoint that was tested was for the EPF / CTRLO go live on the following path:

```
/v1/epf/person-details/$ctrlo_crn/$event_number
```

The upstream API is:

https://effective-proposal-framework-and-delius-dev.hmpps.service.justice.gov.uk

## Results

The tests ran for 1 minute with a few curl requests in parallel from a local machine.
We managed to achieve roughly `2000 successful requests per minute`.
That equals roughly `33 requests per second`.

The outcome of this test was seen as a success and more than enough to cope with expected traffic for this go live.

## Test script

Prerequisites:

- API Gateway consumer API Key
- Test Delius Case Reference Number
- Test Delius Event number for above case
- Consumer certificate and private in the scripts directory

The test script can be run as follows:
```
./scripts/test_performance.sh $API_KEY $CRN $EVENT_ID
```

To establish the number of requests for a given time period, count the results / timestamps:

```bash
  cat /tmp/result |wc -l
```

