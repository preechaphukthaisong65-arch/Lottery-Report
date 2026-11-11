# Data source and verification policy

## Official result sources

GLO Data Catalog dataset:
- Dataset: https://gdcatalog.glo.or.th/dataset/dataset_c4-9_01
- Latest-results API resource page: https://gdcatalog.glo.or.th/dataset/84c73398-f1c2-42b3-b616-34f670cb8c37/resource/0846af20-af93-498c-91d9-644a885733f7
- Date-based result lookup API resource page: https://gdcatalog.glo.or.th/dataset/dataset_c4-9_01/resource/a4d750a4-f8a0-41f3-b62f-99ad97cd680a
- Official result checking page: https://www.glo.or.th/mission/reward-payment/check-reward
- Official draw schedule FAQ: https://www.glo.or.th/service/faq/award
- GLO result listing page: https://www.glo.or.th/mission/awarding/orderby-time

The catalog describes the dataset as public data and lists a Creative Commons Non-Commercial (Any) license. Its resource pages list access condition as "GLO Public Used". The app is intended for personal, non-commercial use only, without advertising or paid access. This is not a substitute for complying with the full license terms. Any redistribution, commercial use, or monetization requires a separate license review and written clarification from GLO where applicable. Dataset contact listed by the catalog: callcenter@glo.or.th.

## What has been verified

- GLO publishes catalog entries describing API resources for latest results and date-based lookup.
- The public resource detail pages currently expose no concrete request URL, HTTP method, required parameters, response schema, or authentication contract in the page content available for verification.
- The official result-checking page is accessible and provides year, draw-date, and ticket-number inputs.
- GLO FAQ states ordinary draws occur on the 1st and 16th, with stated exceptions and official announcements when dates are moved.
- The official result listing page identifies prize sections including first prize, front-three, last-three, last-two, adjacent first prize, and prize tiers two through five.

## Integration decision

Do not implement a guessed API endpoint or rely on a third-party endpoint as if it were official.

Until GLO provides a verifiable API contract and a live response can be validated:
- The app provides direct links to GLO's official result-checking page and draw-date FAQ.
- The in-app 1st/16th calendar entries remain ESTIMATED until the user checks an official GLO notice.
- A user may manually record a CONFIRMED date only after checking the GLO notice, with the official GLO URL stored as provenance. This is a user-attested confirmation, not an automatic GLO verification.
- When a draw is moved, the user can identify the superseded estimated date; the app removes only that estimated row and reschedules local reminders.
- Local reminders are scheduled for 18:00 Bangkok time on the day before each saved draw date. Android may delay delivery under battery restrictions.
- Automatic result retrieval and automatic official schedule confirmation remain disabled.

To reopen API integration, obtain from GLO the exact endpoint, HTTP method, parameters, response schema, usage limits, and applicable license/terms. Then capture real responses for multiple draw dates and create parser fixtures before enabling automatic saving.

## Schedule source

GLO FAQ: https://www.glo.or.th/service/faq/award

Ordinary dates are the 1st and 16th of each month, subject to exceptions described by GLO and official notices for moved draws. The app seeds ordinary dates for the next 12 months as ESTIMATED only. A user can record a date as CONFIRMED after manually checking a GLO notice and saving its URL. The app does not fetch or validate that announcement automatically.

## Safety requirements

- Keep schedule data and prize-result data as separate records and verification flows.
- Never infer a changed date from an unofficial calendar.
- Never replace a previously verified result with an empty, partial, malformed, or failed response.
- Store the original source URL and the time the response was verified.
- Structural validation is not proof of authenticity. Do not label parsed data official until the source adapter is verified against real GLO responses.
- Do not embed API credentials or private keys in the application.
- Do not scrape or redistribute data in a way that conflicts with source terms.
- The app is independent and is not an official GLO application.
