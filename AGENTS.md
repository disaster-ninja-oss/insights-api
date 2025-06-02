Notes for agents:

1. check endpoints and database schema as described in docs/
2. do not break identation. do not mix tabs/spaces, format the code nicely and consistent
3. when calling IndicatorRepository methods from indicator uploading methots, use jdbcTemplateRW, because it connects to primary DB node and always has actual data, which is necessary for correct indicator uploading
4. don't update README.md with minor code fixes
