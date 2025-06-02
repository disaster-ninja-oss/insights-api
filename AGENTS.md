# Notes for agents

- Check endpoints and database schema as described in `docs/`.
- Do not break indentation.
- Do not mix tabs and spaces.
- Format the code nicely and consistently.
- When calling `IndicatorRepository` methods from indicator uploading methods, use `jdbcTemplateRW`, because it connects to the primary DB node and always has actual data, which is necessary for correct indicator uploading.
- Don't update `README.md` with minor code fixes.
- Write insightful code comments.
- Write enough comments so you can deduce what was a requirement in the future.
- Fix everything in the `docs/` folder to match reality.
- When refactoring to move a feature, don't forget to remove the original code path.
- Add enough debug logs so you can find out what's wrong but not be overwhelmed when something does not work as expected.
