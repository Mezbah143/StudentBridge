# Paper.design UI Specification

Last updated: 2026-05-13

## Goal

Use this file as the source content for building StudentBridge web app mockups in Paper.design.

The mockups should be simple enough to implement using the current frontend stack:

- HTML
- CSS
- JavaScript

Do not redesign the project as Node.js, React, or another stack.

## Global Layout

### Navigation

Use a consistent top navigation on web screens:

- Left: StudentBridge wordmark
- Center/right: Home, Jobs, Login, Register
- Primary CTA: Find Jobs
- Secondary CTA: Post a Job

### Page Width

- Desktop content max width: 1120px
- Tablet: 2-column layouts collapse as needed
- Mobile: one column, full-width buttons, stacked filters

### Component Style

| Component | Style |
|---|---|
| Button | 8px radius, 44-48px height, bold label |
| Card | White background, light border, subtle shadow |
| Input | Label above input, 44px minimum height |
| Tags | Small rounded labels with clear text |
| Alerts | Clear success/error color with short text |

## Screen 1: Homepage

### Purpose

Explain the StudentBridge problem and direct students/employers into the core flow.

### Layout

1. Header navigation
2. Hero section
3. Three user sections
4. How-it-works steps
5. Evidence/links strip
6. Footer

### Hero Copy

Headline:

```text
Find part-time jobs in Korea with confidence
```

Supporting text:

```text
StudentBridge helps international students discover student-friendly part-time jobs and connect with local employers.
```

CTA buttons:

- Find Jobs
- Post a Job

### User Sections

| Section | Content |
|---|---|
| For Students | Search jobs by city, job type, and student-friendly requirements. |
| For Employers | Share part-time openings with motivated international students. |
| For Community | Support students with clearer job information and safer workflows. |

### How It Works

1. Search jobs
2. Register or log in
3. Apply online
4. Connect with employers

### Evidence Strip

Use placeholders:

- Live demo: `[TODO: Add live demo link]`
- Demo video: `[TODO: Add demo video link]`
- Sprint evidence: `[TODO: Add sprint packet link]`

## Screen 2: Job Search Page

### Purpose

Let students quickly find part-time jobs that match location, schedule, and category needs.

### Layout

1. Page title: Search Student-Friendly Jobs
2. Search/filter panel
3. Job result cards
4. Empty state

### Search Fields

- Job title keyword
- City/Region
- Category
- Job type

### Job Card Content

Use this card structure:

```text
Cafe Assistant
CoffeeDay
Ulsan - Part-time
W10,000/hr
Tags: Beginner Friendly, Flexible, English Friendly
Serve drinks and assist customers.
[Apply]
```

### Empty State

```text
No jobs found. Try another keyword or city.
```

## Screen 3: Register Page

### Purpose

Allow students and employers to create accounts.

### Fields

- Full name
- Email address
- Phone number
- Password
- Confirm password
- Account type: Student / Employer
- Terms checkbox

Primary button:

```text
Create Account
```

Secondary link:

```text
Already have an account? Login
```

## Screen 4: Login Page

### Purpose

Allow users to access StudentBridge with email and password.

### Fields

- Email address
- Password
- Show/hide password control

Primary button:

```text
Login
```

Error state:

```text
Invalid email or password.
```

Secondary link:

```text
Create an account
```

## Screen 5: Job Application Page

### Purpose

Show the planned final student application flow.

### Layout

1. Job summary card
2. Application form
3. Submit button
4. Privacy/helper note

### Job Summary Example

```text
Cafe Assistant
CoffeeDay - Ulsan
Part-time - W10,000/hr
```

### Fields

- Applicant name
- Email address
- Message to employer
- Resume placeholder: `[Future feature: resume upload]`

Primary button:

```text
Submit Application
```

Helper note:

```text
Application storage will use Java Servlet + MySQL in the final MVP flow.
```

## Screen 6: Employer Post Job Page

### Purpose

Allow employers to create a clear part-time job listing.

### Fields

- Job title
- City/Region
- Category
- Job type
- Salary
- Description

Primary button:

```text
Post Job
```

Helper text:

```text
Postings should be clear, student-friendly, and legal for part-time work.
```

## Screen 7: Confirmation / Success State

Use for registration, login, job application, and job posting.

### Success Message

```text
Success! Your information has been submitted.
```

### Buttons

- Back to Jobs
- Go Home

## Mobile Rules

- Stack filters vertically.
- Keep labels visible above inputs.
- Use full-width action buttons.
- Reduce hero text size.
- Job cards should remain one card per row.
- Keep bottom spacing so the Apply button is easy to tap.

## Implementation Notes

- Current frontend can implement this with existing HTML/CSS/JavaScript.
- Backend screens should continue using Java Servlet form submissions.
- Database-backed job and application flows should use MySQL.
- Do not add fake live data, fake screenshots, or fake evidence.
