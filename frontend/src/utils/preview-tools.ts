export type PreviewToolDefinition = {
  slug: string
  title: string
  shortTitle: string
  description: string
  fileName: string
  type: string
  icon: string
  category:
    | 'Docs'
    | 'Data'
    | 'Visual'
    | 'API'
    | 'Infra'
    | 'Operations'
    | 'Automation'
    | 'Security'
    | 'Terminal'
  keywords: string[]
  example: string
}

export const previewTools: PreviewToolDefinition[] = [
  {
    slug: 'markdown-editor',
    title: 'Markdown Editor & Article Preview',
    shortTitle: 'Markdown',
    description:
      'Write Markdown on the left and see the rendered article preview on the right before you decide to publish it.',
    fileName: 'article.md',
    type: 'markdown',
    icon: 'file-text',
    category: 'Docs',
    keywords: ['markdown', 'article', 'reader mode', 'documentation'],
    example: `# Pastefy Tooling

Build a polished article before you publish it.

## Why this is useful
- Draft docs with a clean article preview
- Share snippets as readable content
- Paste only when you are ready

\`\`\`ts
console.log('hello from pastefy')
\`\`\`
`,
  },
  {
    slug: 'json-viewer',
    title: 'JSON Viewer & Formatter',
    shortTitle: 'JSON',
    description:
      'Inspect, format, and preview JSON structures with a live editor and the richer JSON tools side by side.',
    fileName: 'payload.json',
    type: 'json',
    icon: 'braces',
    category: 'Data',
    keywords: ['json', 'viewer', 'formatter', 'api payload'],
    example: `{
  "service": "pastefy",
  "environment": "staging",
  "features": {
    "readerMode": true,
    "presentationMode": true
  },
  "users": [
    { "id": 1, "name": "Ada", "role": "admin" },
    { "id": 2, "name": "Linus", "role": "user" }
  ]
}
`,
  },
  {
    slug: 'html-preview',
    title: 'HTML Preview Sandbox',
    shortTitle: 'HTML',
    description:
      'Prototype HTML snippets in a lightweight split view and render them in the built-in sandbox before sharing.',
    fileName: 'index.html',
    type: 'html',
    icon: 'world-code',
    category: 'Visual',
    keywords: ['html', 'preview', 'sandbox', 'component playground'],
    example: `<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Pastefy HTML Preview</title>
    <style>
      body { font-family: sans-serif; padding: 2rem; background: linear-gradient(135deg, #f7f3ff, #eef8ff); }
      .card { background: white; padding: 1.5rem; border-radius: 18px; box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12); }
    </style>
  </head>
  <body>
    <article class="card">
      <h1>Hello from Pastefy</h1>
      <p>This should render as a mini webpage.</p>
    </article>
  </body>
</html>
`,
  },
  {
    slug: 'xml-viewer',
    title: 'XML Viewer & Inspector',
    shortTitle: 'XML',
    description:
      'Load XML, inspect nested nodes, and validate the rendered structure in a tool page built for config and document files.',
    fileName: 'pom.xml',
    type: 'xml',
    icon: 'file-code',
    category: 'Data',
    keywords: ['xml', 'inspector', 'pom', 'config'],
    example: `<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>app.pastefy</groupId>
  <artifactId>pastefy-demo</artifactId>
  <version>1.0.0</version>

  <properties>
    <java.version>21</java.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
  </dependencies>
</project>
`,
  },
  {
    slug: 'yaml-viewer',
    title: 'YAML Viewer & Tree Explorer',
    shortTitle: 'YAML',
    description:
      'Explore YAML like a structured document with collapsible sections, a live editor, and an instant preview.',
    fileName: 'docker-compose.yml',
    type: 'yaml',
    icon: 'list-tree',
    category: 'Data',
    keywords: ['yaml', 'viewer', 'tree', 'compose'],
    example: `version: "3.9"

services:
  web:
    image: pastefy/web:latest
    ports:
      - "8080:8080"
    environment:
      APP_ENV: production
      LOG_LEVEL: info

  redis:
    image: redis:7
    restart: unless-stopped
`,
  },
  {
    slug: 'csv-viewer',
    title: 'CSV Table Preview',
    shortTitle: 'CSV',
    description:
      'Preview CSV data in a readable table layout and tweak the source without needing to create a paste first.',
    fileName: 'report.csv',
    type: 'csv',
    icon: 'table',
    category: 'Data',
    keywords: ['csv', 'table', 'spreadsheet'],
    example: `name,role,team
Ada,admin,platform
Linus,user,backend
Grace,user,frontend
`,
  },
  {
    slug: 'mermaid-live-editor',
    title: 'Mermaid Diagram Live Preview',
    shortTitle: 'Mermaid',
    description:
      'Edit Mermaid diagrams with a live visual render so architecture and flowchart snippets are share-ready faster.',
    fileName: 'architecture.mermaid',
    type: 'mermaid',
    icon: 'chart-arrows-vertical',
    category: 'Visual',
    keywords: ['mermaid', 'diagram', 'flowchart', 'architecture'],
    example: `graph TD
  User --> Frontend
  Frontend --> Backend
  Backend --> Postgres
  Backend --> Redis
`,
  },
  {
    slug: 'geojson-viewer',
    title: 'GeoJSON Map Preview',
    shortTitle: 'GeoJSON',
    description:
      'Preview spatial data with a map-first experience while editing raw GeoJSON side by side.',
    fileName: 'map.geojson',
    type: 'geojson',
    icon: 'map-2',
    category: 'Visual',
    keywords: ['geojson', 'map', 'location data'],
    example: `{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "properties": {
        "name": "Berlin"
      },
      "geometry": {
        "type": "Point",
        "coordinates": [13.4050, 52.5200]
      }
    }
  ]
}
`,
  },
  {
    slug: 'svg-viewer',
    title: 'SVG Viewer',
    shortTitle: 'SVG',
    description:
      'Test SVG snippets in a clean split layout and see the resulting vector preview immediately.',
    fileName: 'diagram.svg',
    type: 'xml',
    icon: 'vector',
    category: 'Visual',
    keywords: ['svg', 'vector', 'icon', 'diagram'],
    example: `<svg width="300" height="120" xmlns="http://www.w3.org/2000/svg">
  <rect x="10" y="10" width="280" height="100" rx="16" fill="#111827"/>
  <text x="150" y="68" font-size="24" text-anchor="middle" fill="#ffffff">Pastefy SVG</text>
</svg>
`,
  },
  {
    slug: 'diff-viewer',
    title: 'Diff Viewer',
    shortTitle: 'Diff',
    description:
      'Review patches and code changes in a dedicated diff preview page that behaves more like a quick review tool.',
    fileName: 'changes.diff',
    type: 'diff',
    icon: 'git-compare',
    category: 'Docs',
    keywords: ['diff', 'patch', 'review'],
    example: `diff --git a/app.ts b/app.ts
index 1111111..2222222 100644
--- a/app.ts
+++ b/app.ts
@@ -1,4 +1,5 @@
 console.log("hello")
+console.log("pastefy")
 const enabled = true
`,
  },
  {
    slug: 'env-viewer',
    title: '.env / Config Viewer',
    shortTitle: 'Config',
    description:
      'Inspect environment-style config files in a dedicated preview tool that is useful for app settings and deployment data.',
    fileName: '.env',
    type: 'properties',
    icon: 'settings-code',
    category: 'Infra',
    keywords: ['env', 'properties', 'ini', 'config'],
    example: `APP_NAME=Pastefy
APP_ENV=production
APP_DEBUG=false
DB_HOST=postgres
DB_PORT=5432
DB_USER=pastefy
DB_PASSWORD=supersecret
REDIS_HOST=redis
`,
  },
  {
    slug: 'terraform-viewer',
    title: 'Terraform / HCL Preview',
    shortTitle: 'Terraform',
    description:
      'Edit Terraform and HCL files with a dedicated infrastructure preview, then publish the snippet when it is ready.',
    fileName: 'main.tf',
    type: 'hcl',
    icon: 'building-warehouse',
    category: 'Infra',
    keywords: ['terraform', 'hcl', 'infrastructure as code'],
    example: `terraform {
  required_version = ">= 1.5.0"
}

provider "aws" {
  region = "eu-central-1"
}

resource "aws_s3_bucket" "assets" {
  bucket = "pastefy-assets-demo"
}

variable "environment" {
  type    = string
  default = "staging"
}
`,
  },
  {
    slug: 'log-viewer',
    title: 'Log Viewer',
    shortTitle: 'Logs',
    description:
      'Paste application or server logs into a live viewer to filter and inspect them before creating a shareable paste.',
    fileName: 'app.log',
    type: 'log',
    icon: 'logs',
    category: 'Operations',
    keywords: ['logs', 'viewer', 'server logs'],
    example: `2026-03-20 10:00:01 INFO  Starting Pastefy
2026-03-20 10:00:02 INFO  Connected to postgres
2026-03-20 10:00:03 WARN  Redis latency is high
2026-03-20 10:00:04 ERROR Failed to index paste 4a8f1
2026-03-20 10:00:05 DEBUG Retrying elastic sync
`,
  },
  {
    slug: 'http-viewer',
    title: 'HTTP / cURL Preview',
    shortTitle: 'HTTP',
    description:
      'Compose HTTP requests or cURL calls and inspect them in a dedicated request preview before publishing or sharing.',
    fileName: 'request.http',
    type: 'http',
    icon: 'api',
    category: 'API',
    keywords: ['http', 'curl', 'rest', 'api request'],
    example: `POST https://api.pastefy.app/api/v2/paste
Authorization: Bearer demo-token
Content-Type: application/json

{
  "title": "hello.txt",
  "content": "Hello World",
  "visibility": "UNLISTED"
}
`,
  },
  {
    slug: 'github-actions-viewer',
    title: 'GitHub Actions Workflow Preview',
    shortTitle: 'Actions',
    description:
      'Inspect workflow YAML with a specialized GitHub Actions preview that highlights jobs and steps while you edit.',
    fileName: 'deploy.workflow.yml',
    type: 'yaml',
    icon: 'brand-github',
    category: 'Automation',
    keywords: ['github actions', 'workflow', 'ci'],
    example: `name: Deploy

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      - name: Install
        run: npm ci
      - name: Test
        run: npm test
`,
  },
  {
    slug: 'ansible-viewer',
    title: 'Ansible Playbook Preview',
    shortTitle: 'Ansible',
    description:
      'Work on Ansible playbooks in a split editor and preview tasks, roles, and hosts in a clearer operational view.',
    fileName: 'playbook.yml',
    type: 'yaml',
    icon: 'server-cog',
    category: 'Automation',
    keywords: ['ansible', 'playbook', 'automation'],
    example: `- name: Deploy Pastefy
  hosts: app_servers
  become: true

  roles:
    - docker
    - pastefy

  tasks:
    - name: Pull latest image
      ansible.builtin.shell: docker pull pastefy/backend:latest

    - name: Restart service
      ansible.builtin.service:
        name: pastefy
        state: restarted
`,
  },
  {
    slug: 'prometheus-rules-viewer',
    title: 'Prometheus / Alert Rules Preview',
    shortTitle: 'Alerts',
    description:
      'Preview monitoring rules and alert groups in a page tailored to observability configs and alert definitions.',
    fileName: 'alerts.yml',
    type: 'yaml',
    icon: 'activity-heartbeat',
    category: 'Operations',
    keywords: ['prometheus', 'grafana', 'alerts', 'monitoring'],
    example: `groups:
  - name: pastefy-alerts
    rules:
      - alert: PastefyHighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 1
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: High error rate detected
`,
  },
  {
    slug: 'regex-lab',
    title: 'Regex Tester',
    shortTitle: 'Regex',
    description:
      'Prototype, inspect, and share regular expressions with a dedicated regex-focused preview.',
    fileName: 'pattern.regex',
    type: 'regex',
    icon: 'regex',
    category: 'Security',
    keywords: ['regex', 'pattern', 'validation', 'search'],
    example: `^(?<protocol>https?):\\/\\/(?<host>[\\w.-]+)(?<path>\\/.*)?$`,
  },
  {
    slug: 'terminal-cast-viewer',
    title: 'Terminal Cast Preview',
    shortTitle: 'Terminal Cast',
    description:
      'Preview recorded terminal sessions and shell demos before you publish them as pastes.',
    fileName: 'session.cast',
    type: 'cast',
    icon: 'terminal-2',
    category: 'Terminal',
    keywords: ['terminal', 'asciinema', 'cast', 'shell demo'],
    example: `{"version": 2, "width": 80, "height": 24, "timestamp": 1700000000, "env": {"SHELL": "/bin/bash", "TERM": "xterm-256color"}}
[0.1, "o", "$ echo hello from pastefy\\r\\nhello from pastefy\\r\\n"]
[0.8, "o", "$ ls\\r\\nREADME.md  src  package.json\\r\\n"]`,
  },
  {
    slug: 'calendar-viewer',
    title: 'ICS Calendar Preview',
    shortTitle: 'Calendar',
    description:
      'Inspect calendar invites and event schedules in a quick reader-friendly preview.',
    fileName: 'calendar.ics',
    type: 'ics',
    icon: 'calendar-event',
    category: 'Docs',
    keywords: ['ics', 'calendar', 'events', 'schedule'],
    example: `BEGIN:VCALENDAR
VERSION:2.0
BEGIN:VEVENT
UID:pastefy-demo
DTSTAMP:20260317T120000Z
DTSTART:20260318T090000Z
DTEND:20260318T100000Z
SUMMARY:Pastefy Demo
DESCRIPTION:Preview an ICS file in Pastefy tools.
END:VEVENT
END:VCALENDAR`,
  },
]

export function findPreviewTool(slug?: string) {
  return previewTools.find((tool) => tool.slug === slug)
}
