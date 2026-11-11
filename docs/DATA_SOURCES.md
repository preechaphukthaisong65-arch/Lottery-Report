# Data source and verification policy

## Official result source

The Government Lottery Office (GLO) Data Catalog publishes the dataset "ข้อมูลผลการออกรางวัลสลากกินแบ่งรัฐบาล" and documents API resources for latest results and date-based result lookup:

- Dataset: https://gdcatalog.glo.or.th/dataset/dataset_c4-9_01
- Official result checking: https://www.glo.or.th/mission/reward-payment/check-reward
- GLO FAQ: https://www.glo.or.th/service/faq/award

The catalog identifies the dataset as public data and lists a Creative Commons Non-Commercial (Any) license. The app must remain non-commercial unless GLO provides written permission or a license that allows the intended use. Do not add advertising, paid access, or commercial redistribution based on this dataset without resolving the license first.

## Schedule source

GLO's FAQ describes ordinary draw dates as the 1st and 16th of each month, with exceptions and official announcements when dates are moved. The app now seeds these ordinary dates for the next 12 months as ESTIMATED only. It does not infer special dates, holiday moves, or official confirmation. A date becomes CONFIRMED only after an attributable GLO announcement is retrieved and validated.

## Result API integration status

The GLO Data Catalog lists API resources for latest results and date-based lookup. The resource detail pages currently expose no machine-readable endpoint/schema in the catalog page itself. A candidate endpoint found in third-party implementations is not treated as a verified contract. The application must not mark results official until the request method, parameters, response schema, and provenance have been verified against a live GLO response and tested with captured fixtures.

## Safety requirements

- Keep schedule data and prize-result data as separate records and verification flows.
- Never infer a changed date from an unofficial calendar.
- Never replace a previously verified result with an empty, partial, malformed, or failed response.
- Store the original source URL and the time the response was verified.
- Structural validation is not proof of authenticity. The app must retain source provenance and must not label parsed data official until the source adapter has been verified against real GLO responses.
- Do not embed API credentials or private keys in the application.
- Do not scrape or redistribute data in a way that conflicts with the source terms.
- The app is independent and is not an official GLO application.
