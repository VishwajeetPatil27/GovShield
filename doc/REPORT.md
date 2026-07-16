# GovShield Project Report

## 1. Introduction

GovShield is a governance support platform designed to reduce fraud, improve transparency, and connect citizen services with monitoring tools for officers and auditors.

## 2. Problem Statement

Government welfare and monitoring systems often suffer from:

- duplicate or ineligible benefit claims
- weak audit visibility
- poor transparency in public projects
- fragmented citizen data

GovShield addresses these issues with a unified platform.

## 3. Objectives

- centralize beneficiary and scheme data
- support secure authentication for multiple user roles
- validate eligibility for schemes
- detect suspicious patterns and conflicts
- track projects and alerts
- maintain audit logs

## 4. System Design

### Frontend

The frontend is a static app with login pages and dashboards for:

- citizens
- officers
- auditors

### Backend

The backend exposes REST endpoints for:

- authentication
- citizens
- schemes
- eligibility
- documents
- fraud detection
- project monitoring
- audits

### Database

The MySQL database stores:

- employees
- citizens
- schemes
- enrollments
- documents
- projects
- alerts
- audit logs

## 5. Execution Summary

The application can be run locally with:

1. MySQL database import from `database/`
2. Spring Boot backend from `backend/`
3. Static frontend from `frontend/`

## 6. Notes

- Demo employee credentials are listed in the login page and setup guide.
- The backend health endpoint is `/api/health`.
- The backend login endpoint is `/api/auth/login`.

