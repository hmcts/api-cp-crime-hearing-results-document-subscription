# Skill: Create PR

## Trigger

Invoke this skill when a user asks to:
- Create a pull request, raise a PR, or open a PR
- Address or action code review comments on an existing PR
- Update a PR based on reviewer feedback

Invocation command: `/create-pr`

---

## Process

### Step 1 — Gather git context

Run these commands and collect all output before proceeding:

```bash
git branch --show-current
git log origin/main..HEAD --oneline
git diff origin/main..HEAD --stat
git diff origin/main..HEAD
git log origin/main..HEAD --format="%B" | head -100
```

### Step 2 — Extract the JIRA ticket

From the branch name, extract the JIRA ticket reference using these rules:

- Branch patterns: `dev/AMP-346`, `dev/AMP346`, `dev/AMP-346-description`, `AMP-346`
- Normalise to uppercase with hyphen: `AMP-346` (not `AMP346`)
- If no ticket is found in the branch name, scan the most recent commit message for a pattern like `AMP-NNN`
- If still not found, prompt the user: *"I couldn't find a JIRA ticket in the branch name. What's the ticket reference (e.g. AMP-433)?"*

Construct the JIRA URL:
```
https://tools.hmcts.net/jira/browse/<TICKET>
```

### Step 3 — Verify JIRA ticket is In Progress

Using the extracted ticket, check its status via the JIRA REST API:

```bash
JIRA_STATUS=$(curl -s -u "$JIRA_USER:$JIRA_TOKEN" \
  "https://tools.hmcts.net/jira/rest/api/2/issue/<TICKET>" \
  | jq -r '.fields.status.name')
echo "$JIRA_STATUS"
```

- If the status is **`"In Progress"`**, proceed.
- If the status is **anything else** (e.g. `"To Do"`, `"In Review"`, `"Done"`), warn the user:
  *"The JIRA ticket `<TICKET>` is currently `<STATUS>`, not `In Progress`. Do you want to move it to In Progress before raising the PR, or proceed anyway?"*
- If the API call fails (e.g. missing credentials or network error), warn the user and ask whether to proceed:
  *"I couldn't check the status of `<TICKET>` — JIRA API returned an error. Do you want to proceed anyway?"*

### Step 4 — Understand the changes

From the git diff and commit log, determine:

**What changed** — concrete list of files/components modified:
- Group by type: e.g. build files, test infrastructure, application code, config, docker
- Be specific: "Added `subscriptionKey` security scheme to `components/securitySchemes` in the OpenAPI spec" not "Updated OpenAPI spec"

**Why it's needed** — the business or technical rationale:
- Look for clues in commit messages, branch name, and the nature of the diff
- If the rationale isn't clear from the code, ask the user: *"Can you give me a one-line summary of why this change is needed, for the PR description?"*

### Step 5 — Draft the PR

**Title format:**
```
<TICKET> <concise description of the change in plain English>
```
Example: `AMP-346 Instil subscription key in client request to API`

Keep the title under 72 characters. Do not include "changes" as the only description — be specific.

**Body format:**

```markdown
## JIRA
[<TICKET>](https://tools.hmcts.net/jira/browse/<TICKET>)

## What changed
- <specific change 1>
- <specific change 2>
- <specific change 3>
...

## Why it's needed
<1-3 sentences explaining the motivation — technical debt, bug fix, new requirement, etc.>

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

### Step 6 — Check for uncommitted changes

Before creating the PR, run:
```bash
git status
git log origin/main..HEAD --oneline
```

- If there are **uncommitted changes**, warn the user: *"You have uncommitted changes. Do you want me to commit them first, or create the PR with only the pushed commits?"*
- If the **branch hasn't been pushed**, run `git push -u origin <branch>` first
- If there are **no commits ahead of main**, stop and tell the user: *"There are no commits ahead of main on this branch. Nothing to PR."*

### Step 7 — Check for an existing open PR

Before creating a new PR, check if one already exists for the current branch:

```bash
gh pr view --json number,url,title,state 2>/dev/null
```

- If an **open PR already exists**, switch to **Amend PR mode** (see below) instead of creating a new one.
- If no PR exists, proceed with creation as normal.

---

## Amend PR Mode

Triggered when an open PR already exists for the current branch, or the user asks to address review comments.

### Amend Step 1 — Fetch review comments

```bash
gh pr view --json number,url
gh api repos/hmcts/api-cp-crime-hearing-results-document-subscription/pulls/<PR_NUMBER>/reviews --jq '.[].body'
gh pr comments --json body,author
gh api repos/hmcts/api-cp-crime-hearing-results-document-subscription/pulls/<PR_NUMBER>/comments --jq '.[] | {path: .path, line: .original_line, comment: .body, author: .user.login}'
```

List all unresolved review comments grouped by file. Present a summary to the user:
- *"I found N review comments. Here's a summary: ..."*

The developer is responsible for addressing the comments. Once they have made the fixes, proceed to Amend Step 2.

### Amend Step 2 — Confirm fixes are ready

Ask the user: *"Have you made the fixes? I'll commit and push them now."*

Wait for confirmation before proceeding.

### Amend Step 3 — Commit the fixes

Stage and commit only the files changed in response to review feedback:

```bash
git add <specific files>
git commit -m "<TICKET>: address PR review comments — <brief summary>"
```

Do not amend existing commits. Always create a new commit.

### Amend Step 4 — Push and update the PR

```bash
git push
```

Then update the PR description to note the review comments have been addressed. Append to the existing body:

```markdown
## Review changes (<date>)
- <what was changed in response to review>
```

Use:
```bash
gh pr edit --body "$(cat <<'EOF'
<updated body>
EOF
)"
```

### Amend Step 5 — Re-request review (optional)

Ask the user: *"Do you want me to re-request review from the original reviewers?"*

If yes:
```bash
gh pr edit --add-reviewer <reviewer-login>
```

---

### Step 8 — Create the PR

**Create the PR** and capture the returned URL:

```bash
PR_URL=$(gh pr create \
  --title "<title>" \
  --body "$(cat <<'EOF'
<body>
EOF
)" \
  --base main)
echo "$PR_URL"
```

## Rules

- **Never use "changes" as the sole PR title descriptor.** Always describe what the change actually does.
- **Always include the JIRA link** — if the ticket can't be found, ask before creating the PR.
- **Never force-push** or amend existing commits without explicit user instruction.
- **Review responses must be new commits** — never amend existing commits to address review comments; always commit fresh so the reviewer can see what changed.
- **Always confirm** before pushing the branch if it hasn't been pushed yet.
- If `gh` CLI is not authenticated, tell the user to run `gh auth login` first.

---

## Example output

**Branch:** `dev/AMP-346`
**Extracted ticket:** `AMP-346`

**Title:**
```
AMP-346 Add subscription key security scheme and 401/403 responses to OpenAPI spec
```

**Body:**
```markdown
## JIRA
[AMP-346](https://tools.hmcts.net/jira/browse/AMP-346)

## What changed
- Added `subscriptionKey` security scheme (`Ocp-Apim-Subscription-Key` header) to `components/securitySchemes` in the OpenAPI spec
- Updated global security to require both `bearerAuth` and `subscriptionKey`
- Added `401` and `403` responses to all endpoints that were missing them, with descriptive messages
- Added `SubscriptionKeySecurityTest` to verify the spec defines the subscription key scheme, global security requirements, and that all endpoints declare 401/403 responses

## Why it's needed
The API must enforce APIM subscription key authentication alongside JWT bearer auth. The OpenAPI spec was missing the `subscriptionKey` security scheme and was lacking 401/403 response definitions on several endpoints, which left the contract incomplete and untested.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```