# NegRendererAgent

A Java agent to allow negative item stacks to render their count from indev 20100124-2 to 14w32d

## Installing

1) Download the latest jar from the [latest releases](https://github.com/Captain-S0L0/negrendereragent/releases)
2) Place the jar into your .minecraft folder
3) Add `-javaagent:"negrendereragent-1.0.0"` to your Java arguments (change the jar version as appropriate)
4) Enjoy!

## Features

All the agent does is modify a single Java bytecode within the item rendering code from "if count > 1 then render count" to "if count != 1 then render count"

![](https://raw.githubusercontent.com/Captain-S0L0/negrendereragent/master/src/main/resources/NegRendererAgentExampleAlpha.png)

As a result of some clever search methods to identify the ofbuscated classes, this agent (should) support all versions from Indev 20100124-2 (the first version where understacked items are available) to 14w32d (the last version before understacked items render their count in vanilla).

If it doesn't work in any version between, please create an issue so I can fix it!!! Versions outside of that range are not going to be supported.
