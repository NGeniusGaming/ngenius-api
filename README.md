# NGenius API
> An API for handling data with the ngenius-teamview application!

## Running

To run this application, you'll need to provide some Twitch credentials! 
Never fear though, anyone can get their own twitch credentials!

This application connects to Twitch via OAuth, therefore, you'll need to provide this application
your credentials!

Your environment should contain: 

`TWITCH_CLIENT_ID` - set to your twitch client id.

`TWITCH_CLIENT_SECRET`- set to your twitch client secret.

[You can learn more about authenticating with Twitch in their developer docs](https://dev.twitch.tv/docs/authentication/#types-of-tokens).

#### Common Errors

If you get a stacktrace along these lines, you did not properly set your configuration environment variables.

```
There was an unexpected error (type=Internal Server Error, status=500).
Not enough variable values available to expand 'TWITCH_CLIENT_ID'
```
