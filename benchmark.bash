#!/bin/bash

# Define the frameworks to permute
frameworks=("ktor" "http4k")

# Define the client engines for Ktor and http4k
ktor_client_engines=("apache" "jetty" "okhttp" "cio")
http4k_client_engines=("apache" "jetty" "okhttp" "helidon")

# Define the server engines for Ktor and http4k
ktor_server_engines=("netty" "jetty" "tomcat" "cio")
http4k_server_engines=("apache" "jetty" "undertow" "helidon")

# Loop through all combinations of server_framework, client_framework, server_engine, and client_engine
for server_framework in "${frameworks[@]}"; do
    for client_framework in "${frameworks[@]}"; do

        # Determine the correct client engines and server engines arrays
        if [ "$server_framework" == "ktor" ]; then
            server_engines=("${ktor_server_engines[@]}")
        elif [ "$server_framework" == "http4k" ]; then
            server_engines=("${http4k_server_engines[@]}")
        fi

        if [ "$client_framework" == "ktor" ]; then
            client_engines=("${ktor_client_engines[@]}")
        elif [ "$client_framework" == "http4k" ]; then
            client_engines=("${http4k_client_engines[@]}")
        fi

        # Loop through all server engines
        for server_engine in "${server_engines[@]}"; do
            # Loop through all client engines
            for client_engine in "${client_engines[@]}"; do
                # Create a CSV output filename based on the frameworks and engines
                csv_output="output_server_${server_framework}_${server_engine}_client_${client_framework}_${client_engine}.csv"

                # Run the Java command with server_engine and client_engine
                echo "Running with server_framework=${server_framework}, server_engine=${server_engine}, client_framework=${client_framework}, client_engine=${client_engine}"
                java -jar target/renaissance-gpl-0.17.0.jar \
                    --csv "$csv_output" \
                    -o workload_count="50" \
                    -o server_framework="$server_framework" \
                    -o server_engine="$server_engine" \
                    -o client_framework="$client_framework" \
                    -o client_engine="$client_engine" \
                    kotlin-web

                # Check if the command was successful
                if [ $? -ne 0 ]; then
                    echo "Error: Command failed with server_framework=$server_framework, server_engine=$server_engine, client_framework=$client_framework, client_engine=$client_engine"
                fi
            done
        done
    done
done
