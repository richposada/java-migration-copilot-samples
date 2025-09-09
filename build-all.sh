#!/bin/bash
set -e

echo "Java Migration Copilot Samples - Build Verification Script"
echo "=========================================================="

# Check Java version
echo "Java version:"
java -version

echo ""
echo "Building all Maven projects..."
echo ""

# Array of Maven projects
MAVEN_PROJECTS=(
    "mi-sql-public-demo"
    "rabbitmq-sender"
    "todo-web-api-use-oracle-db"
    "asset-manager"
)

FAILED_PROJECTS=()
SUCCESSFUL_PROJECTS=()

for project in "${MAVEN_PROJECTS[@]}"; do
    echo "=== Building $project ==="
    if [ -d "$project" ]; then
        cd "$project"
        if mvn clean compile -q; then
            echo "✅ $project - SUCCESS"
            SUCCESSFUL_PROJECTS+=("$project")
        else
            echo "❌ $project - FAILED"
            FAILED_PROJECTS+=("$project")
        fi
        cd ..
    else
        echo "⚠️  $project - DIRECTORY NOT FOUND"
        FAILED_PROJECTS+=("$project")
    fi
    echo ""
done

echo "=========================================================="
echo "BUILD SUMMARY"
echo "=========================================================="
echo "Successful projects: ${#SUCCESSFUL_PROJECTS[@]}"
for project in "${SUCCESSFUL_PROJECTS[@]}"; do
    echo "  ✅ $project"
done

if [ ${#FAILED_PROJECTS[@]} -gt 0 ]; then
    echo ""
    echo "Failed projects: ${#FAILED_PROJECTS[@]}"
    for project in "${FAILED_PROJECTS[@]}"; do
        echo "  ❌ $project"
    done
    echo ""
    echo "Build completed with failures!"
    exit 1
else
    echo ""
    echo "All projects built successfully! 🎉"
    exit 0
fi