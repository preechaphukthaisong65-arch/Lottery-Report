# Data source and verification policy

## Official result source

The Government Lottery Office (GLO) Data Catalog publishes the dataset "ข้อมูลผลการออกรางวัลสลากกินแบ่งรัฐบาล" and documents API resources for latest results and date-based result lookup:

- Dataset: https://gdcatalog.glo.or.th/dataset/dataset_c4-9_01
- Official result checking: https://www.glo.or.th/mission/reward-payment/check-reward
- GLO FAQ: https://www.glo.or.th/service/faq/award

The dataset page identifies the information as public data and lists a Creative Commons Non-Commercial (Any) license. Any redistribution or commercial use must be reviewed against the actual license terms before release.

## Schedule source

GLO's FAQ describes the ordinary draw dates as the 1st and 16th of each month, with stated exceptions and official announcements when dates are moved. Therefore, a computed calendar is only an estimate. A date must not be labeled officially confirmed unless an attributable GLO announcement or source explicitly confirms it.

## Safety requirements

- Keep schedule data and prize-result data as separate records and verification flows.
- Never infer a changed date from an unofficial calendar.
- Never replace a previously verified result with an empty, partial, malformed, or failed response.
- Store the original source URL and the time the response was verified.
- Structural validation is not proof of authenticity. The app must retain source provenance and must not label parsed data official until the source adapter has been verified against real GLO responses.
- Do not embed API credentials or private keys in the application.
- The app is independent and is not an official GLO application.
