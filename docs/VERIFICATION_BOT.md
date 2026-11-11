# Lottery verification bot

## Current implementation: source monitoring only

The bot checks the GLO result page, GLO draw listing, GLO award FAQ, Sanook, Thairath, and Kapook.

- GLO results: https://www.glo.or.th/mission/reward-payment/check-reward
- GLO draw listing: https://www.glo.or.th/mission/awarding/orderby-time
- GLO award FAQ: https://www.glo.or.th/service/faq/award
- Sanook: https://news.sanook.com/lotto/
- Thairath: https://en.thairath.co.th/lottery/latest-result
- Kapook: https://lottery.kapook.com/

The results scan is scheduled every five minutes from 16:00 to 17:55 Thailand time. The calendar/source scan is scheduled daily at 16:15 Thailand time. A push that changes verifier code also performs an initial results-source scan. GitHub scheduled workflows can be delayed.

Each run records retrieval time, requested/final URL, HTTP status, source class, parser version, SHA-256 of the fetched response, and a short text excerpt. Reports are written to \`data/verification/\`. No user or device data is collected.

## Safety boundary

Version 0.1.0 is a reachability/evidence collector, NOT a lottery-result verifier. It does not yet extract all nine prize categories, normalize draw dates, or compare prize numbers. Therefore \`overall_status\` is always \`INCOMPLETE\` and \`verification_enabled\` is \`false\`. It must not write results or official dates into the app database.

A page being reachable, or multiple sites displaying the same value, is not proof of authenticity. GLO remains the primary authority. Secondary sites are cross-checks and may share upstream data. If a source blocks automated access, returns a challenge page, or changes structure, the bot records \`SOURCE_UNAVAILABLE\` or \`INCOMPLETE\`; it does not bypass access controls.

## Next implementation gates

1. Validate stable source-specific HTML/JSON structures against known completed draw dates.
2. Implement and test exact extraction for all nine categories and the official draw date.
3. Add mismatch/incomplete handling and append-only evidence history.
4. Publish a stable report contract for Android WorkManager polling.
5. Add local notification deduplication and user-visible evidence details in the app.

Do not set \`verification_enabled\` to \`true\` until parser tests cover all prize counts, leading zeroes, duplicates, malformed values, stale dates, missing categories, and source disagreement.
