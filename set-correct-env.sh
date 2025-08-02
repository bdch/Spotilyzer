HOSTNAME=$(hostname)

if [[ "$HOSTNAME" == "bdch" ]]; then
    ./gradlew bootRun -Dgrails.env=laptop
else
    ./gradlew bootRun -Dgrails.env=development
fi
