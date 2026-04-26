# Security Policy

## Supported Versions

Only the latest release is supported for security updates.

## Reporting a Vulnerability

Please do **not** open a public GitHub issue for security vulnerabilities.

Use GitHub's **Private Vulnerability Reporting** instead:

https://github.com/relexx/heratalk/security/advisories/new

This keeps your report confidential between you and the maintainers until a fix
is ready. You will receive an acknowledgement within 72 hours, and we aim to
provide a fix or mitigation plan within 30 days for high-severity issues.

Please include:
- A description of the vulnerability
- Steps to reproduce
- Affected versions if known
- Any proof-of-concept code or data

## Scope

In scope:
- Cryptographic weaknesses in pairing or SRTP implementation
- Bypass of channel-secret enforcement
- Relay peer being able to decrypt forwarded traffic
- Buffer overflows in the JNI audio bridge
- Privilege escalation via the foreground service
- Injection via the `heratalk://` URL scheme

Out of scope:
- Denial of service via malformed packets (expected: packets are dropped cleanly)
- Physical access to a paired device
- Attacks requiring root/administrator access on the target device
- Social engineering against channel members
