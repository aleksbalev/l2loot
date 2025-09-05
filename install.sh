#!/bin/bash

# L2Loot Installation Script for Unix/Linux/macOS
# This script installs the L2Loot CLI tool and sets up the environment

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}L2Loot Setup Script${NC}"
echo "==================="

# Check if Java 17+ is installed
echo "Checking Java installation..."
if ! java -version 2>&1 | grep -qE "(openjdk|java).*version.*\"(1[7-9]|[2-9][0-9])\." ; then
    echo -e "${RED}Error: Java 17 or higher is required but not found.${NC}"
    echo "Please install Java 17+ from:"
    echo "  - Oracle JDK: https://www.oracle.com/java/technologies/downloads/"
    echo "  - OpenJDK: https://adoptium.net/"
    echo "  - Or use your system package manager (e.g., 'brew install openjdk@17' on macOS)"
    exit 1
fi

echo -e "${GREEN}✓ Java found:${NC}"
java -version 2>&1 | head -1

# Get the current directory (project directory)
PROJECT_DIR="$(pwd)"
BIN_DIR="$PROJECT_DIR/bin"

echo "Setting up L2Loot in project directory..."
mkdir -p "$BIN_DIR"
mkdir -p "$PROJECT_DIR/database"

# Build the application if artifacts don't exist
if [ ! -f "app/build/libs/l2loot.jar" ]; then
    echo "Build artifacts not found. Building application..."
    if command -v ./gradlew >/dev/null 2>&1; then
        ./gradlew build
    elif command -v gradle >/dev/null 2>&1; then
        gradle build
    else
        echo -e "${RED}Error: Gradle not found. Please install Gradle or use the included gradlew script.${NC}"
        exit 1
    fi
    
    if [ ! -f "app/build/libs/l2loot.jar" ]; then
        echo -e "${RED}Error: Build failed. Please check for build errors.${NC}"
        exit 1
    fi
fi

# Create wrapper script in project bin directory
WRAPPER_SCRIPT="$BIN_DIR/l2loot"
cat > "$WRAPPER_SCRIPT" << EOF
#!/bin/bash
# L2Loot wrapper script - runs from project directory
PROJECT_ROOT="$PROJECT_DIR"
cd "\$PROJECT_ROOT"
exec java -jar "app/build/libs/l2loot.jar" "\$@"
EOF
chmod +x "$WRAPPER_SCRIPT"

# Initialize database with seed data
echo "Initializing database with NPC and price data..."
cd "$PROJECT_DIR"
if java -jar "app/build/libs/l2loot.jar" --seed-if-empty 2>/dev/null; then
    echo -e "${GREEN}✓ Database initialized successfully${NC}"
else
    echo -e "${YELLOW}Warning: Database initialization had issues. You can retry with: ./bin/l2loot --seed-if-empty${NC}"
fi

echo -e "${GREEN}✓ L2Loot set up successfully in project directory${NC}"

# Add project bin directory to PATH options
echo ""
echo -e "${YELLOW}PATH Configuration Options:${NC}"
echo ""
echo "Option 1: Add project bin directory to your PATH (recommended)"

# Detect shell and provide appropriate instructions
if [ -n "$BASH_VERSION" ]; then
    SHELL_RC="$HOME/.bashrc"
elif [ -n "$ZSH_VERSION" ]; then
    SHELL_RC="$HOME/.zshrc"
else
    SHELL_RC="$HOME/.profile"
fi

echo "  echo 'export PATH=\"$BIN_DIR:\$PATH\"' >> $SHELL_RC"
echo "  source $SHELL_RC"
echo ""
echo "Option 2: Run directly from project directory"
echo "  cd $PROJECT_DIR"
echo "  ./bin/l2loot --help"
echo ""
echo "Option 3: Create an alias"
echo "  echo 'alias l2loot=\"$BIN_DIR/l2loot\"' >> $SHELL_RC"
echo "  source $SHELL_RC"

echo ""
echo -e "${GREEN}Setup complete!${NC}"
echo ""
echo "Quick start:"
echo "  ./bin/l2loot --help                    # Show help"
echo "  ./bin/l2loot farm-analysis --min-level 20 --max-level 40  # Find profitable mobs"
echo "  ./bin/l2loot update-prices             # Update prices after editing seed-data/sellable_items.json"
echo ""
echo -e "${GREEN}Your data files are in the project directory:${NC}"
echo "  - Database: ./database/"
echo "  - Seed data: ./seed-data/"
echo ""
echo "For detailed usage instructions, see the README.md file."
