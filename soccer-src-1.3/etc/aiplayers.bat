
goto %1

:left
java -cp soccer.jar soccer.client.ai.AIPlayers -l 11
goto end

:right
java -cp soccer.jar soccer.client.ai.AIPlayers -r 11
goto end

:all
java -cp soccer.jar soccer.client.ai.AIPlayers -r 11 -l 11
goto end

:end

