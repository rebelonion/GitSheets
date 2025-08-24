# GitSheets

A Kotlin/Native application that monitors Google Sheets for changes and automatically commits the data to a Git repository. GitSheets tracks spreadsheet modifications and maintains a version-controlled history of your data.

## Features

- **Automatic Monitoring**: Polls Google Sheets at configurable intervals for changes
- **Git Integration**: Automatically commits and pushes changes to your repository
- **Flexible Configuration**: Configure via TOML files or environment variables
- **Webhook Notifications**: Optional webhook support for change notifications
- **Docker Support**: Run as a containerized service
- **Native Performance**: Compiled to native executables for optimal performance

## Configuration

GitSheets can be configured using either a TOML configuration file or environment variables.

### Configuration File

Create a `config.toml` file:

```toml
[sheet]
id = "your-google-sheet-id"
index = 0  # Optional: specific sheet tab index
headerRow = -1  # Optional: header row for parsing
titleColumn = -1  # Optional: title column for parsing

[git]
repoUrl = "https://github.com/username/repo.git"
branch = "main"
token = "your-github-token"
authorName = "Sheets Tracker"
authorEmail = "sheets-tracker@example.com"

[webhook]  # Optional
url = "https://your-webhook-url.com"
enabled = true

[general]
dataPath = "./data"
intervalMinutes = 15
```

### Environment Variables

Alternatively, configure using environment variables:

- `GOOGLE_SHEET_ID`: Your Google Sheet ID (required)
- `GIT_REPO_URL`: Git repository URL (required)
- `GIT_TOKEN`: Git authentication token (required)
- `GOOGLE_SHEET_INDEX`: Sheet tab index (optional)
- `HEADER_ROW`: Header row number (optional)
- `TITLE_COLUMN`: Title column number (optional)
- `GIT_BRANCH`: Git branch (default: main)
- `GIT_AUTHOR_NAME`: Git author name (default: Sheets Tracker)
- `GIT_AUTHOR_EMAIL`: Git author email (default: sheets-tracker@example.com)
- `WEBHOOK_URL`: Webhook URL for notifications (optional)
- `WEBHOOK_ENABLED`: Enable/disable webhooks (default: true)
- `DATA_PATH`: Data directory path (default: ./data)
- `INTERVAL_MINUTES`: Check interval in minutes (default: 15)
- `CONFIG_DIR`: Directory containing config.toml file

## Usage

### Command Line Arguments

- `--run-once` or `-o`: Run once instead of continuous monitoring
- `--config <path>` or `-c <path>`: Specify configuration directory

### Examples

```bash
# Run continuously with default config
./gitsheets

# Run once with custom config directory
./gitsheets --config /path/to/config --run-once

# Using environment variables
GOOGLE_SHEET_ID="your-sheet-id" GIT_REPO_URL="your-repo-url" GIT_TOKEN="your-token" ./gitsheets
```

## Docker Usage

### Building

```bash
docker build -t gitsheets .
```

### Running

```bash
# Using environment variables
docker run -e GOOGLE_SHEET_ID="your-sheet-id" \
           -e GIT_REPO_URL="your-repo-url" \
           -e GIT_TOKEN="your-token" \
           gitsheets

# Using config file
docker run -v /path/to/config:/workspace/config \
           gitsheets --config /workspace/config
```

### Docker Compose

```yaml
version: '3.8'
services:
  gitsheets:
    build: .
    environment:
      - GOOGLE_SHEET_ID=your-sheet-id
      - GIT_REPO_URL=your-repo-url
      - GIT_TOKEN=your-token
      - INTERVAL_MINUTES=30
    volumes:
      - ./data:/workspace/data
```

## How It Works

1. **Sheet Monitoring**: GitSheets fetches your Google Sheet data as CSV at regular intervals
2. **Change Detection**: Compares current data with previously stored hash to detect changes
3. **Data Storage**: Saves the CSV data to the configured data directory
4. **Git Operations**: Commits changes and pushes to your Git repository
5. **Notifications**: Optionally sends webhook notifications about changes

## Building from Source

### Requirements

- Kotlin/Native toolchain
- Gradle

### Build Commands

```bash
# Build native executable
./gradlew linkReleaseExecutableNativeApp

# Run tests
./gradlew test

# Build Docker image
docker build -t gitsheets .
```