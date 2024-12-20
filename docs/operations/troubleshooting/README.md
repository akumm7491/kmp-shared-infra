# Troubleshooting Guide

This comprehensive guide covers troubleshooting procedures for the KMP Shared Infrastructure, including common issues, debugging procedures, and resolution strategies.

## System Health Checks

### 1. Kubernetes Health
```bash
# Check node status
kubectl get nodes
kubectl describe node <node-name>

# Check pod status
kubectl get pods --all-namespaces
kubectl describe pod <pod-name>

# Check system components
kubectl get componentstatuses
```

### 2. Application Health
```bash
# Check deployment status
kubectl get deployments
kubectl rollout status deployment/<name>

# Check service status
kubectl get services
kubectl describe service <name>

# Check logs
kubectl logs <pod-name> -c <container-name>
```

## Common Issues

### 1. Pod Issues

#### Pod Pending
- Resource constraints
- Node selector issues
- PVC binding issues
- Scheduling constraints

```bash
# Debug pod issues
kubectl describe pod <pod-name>
kubectl get events --sort-by=.metadata.creationTimestamp
```

#### Pod CrashLoopBackOff
- Application crashes
- Configuration errors
- Resource exhaustion
- Dependency issues

```bash
# Check pod logs
kubectl logs <pod-name> --previous
kubectl describe pod <pod-name>
```

### 2. Network Issues

#### Service Connectivity
- DNS resolution
- Service discovery
- Network policies
- Ingress configuration

```bash
# Debug service issues
kubectl get endpoints <service-name>
kubectl get networkpolicies
kubectl describe ingress <ingress-name>
```

#### Load Balancer Issues
- Health checks
- Backend configuration
- SSL/TLS issues
- Routing problems

### 3. Storage Issues

#### Persistent Volume Issues
- Volume binding
- Storage class
- Capacity issues
- Permission problems

```bash
# Check storage status
kubectl get pv,pvc
kubectl describe pv <pv-name>
kubectl describe pvc <pvc-name>
```

#### Database Issues
- Connection problems
- Performance issues
- Replication lag
- Backup/restore issues

## Debugging Procedures

### 1. Application Debugging

#### Log Analysis
```bash
# Container logs
kubectl logs <pod-name> -c <container-name> --tail=100
kubectl logs <pod-name> -c <container-name> --previous

# System logs
journalctl -u kubelet
```

#### Resource Usage
```bash
# Pod metrics
kubectl top pods
kubectl top nodes

# Detailed resource usage
kubectl describe node <node-name>
```

### 2. Network Debugging

#### DNS Debugging
```bash
# Run DNS debug pod
kubectl run dnsutils --image=gcr.io/kubernetes-e2e-test-images/dnsutils:1.3

# Test DNS resolution
kubectl exec -it dnsutils -- nslookup kubernetes.default
```

#### Network Policy Testing
```bash
# Test network connectivity
kubectl run nginx --image=nginx
kubectl exec -it nginx -- curl <service-name>
```

### 3. Performance Debugging

#### Metrics Analysis
```yaml
# Key metrics to check
- CPU usage
- Memory usage
- Network I/O
- Disk I/O
- Request latency
```

#### Profiling
```bash
# Enable profiling
kubectl port-forward <pod-name> 6060:6060
curl localhost:6060/debug/pprof/
```

## Resolution Strategies

### 1. Pod Issues

#### Resource Constraints
```yaml
# Adjust resource limits
resources:
  requests:
    memory: "64Mi"
    cpu: "250m"
  limits:
    memory: "128Mi"
    cpu: "500m"
```

#### Configuration Issues
```bash
# Check configmaps and secrets
kubectl get configmaps
kubectl get secrets
kubectl describe configmap <name>
```

### 2. Network Issues

#### Service Discovery
```bash
# Verify service DNS
kubectl run -it --rm debug --image=busybox -- nslookup <service-name>

# Check service endpoints
kubectl get endpoints <service-name>
```

#### Load Balancer
```bash
# Check service type
kubectl get service <service-name> -o yaml

# Verify external IP
kubectl get service <service-name> -w
```

### 3. Storage Issues

#### Volume Management
```bash
# Check volume status
kubectl get pv,pvc
kubectl describe pv <pv-name>

# Check storage class
kubectl get storageclass
```

#### Data Recovery
```bash
# Backup data
kubectl exec <pod-name> -- pg_dump -U postgres > backup.sql

# Restore data
kubectl exec -i <pod-name> -- psql -U postgres < backup.sql
```

## Monitoring and Alerts

### 1. Prometheus Queries
```promql
# CPU usage
rate(container_cpu_usage_seconds_total{container!=""}[5m])

# Memory usage
container_memory_usage_bytes{container!=""}

# Disk usage
container_fs_usage_bytes{container!=""}
```

### 2. Alert Investigation
```yaml
# Alert rules
groups:
- name: example
  rules:
  - alert: HighCPUUsage
    expr: container_cpu_usage_seconds_total > 0.8
    for: 5m
```

## Best Practices

### 1. Proactive Monitoring
- Set up comprehensive monitoring
- Configure appropriate alerts
- Regular health checks
- Performance baselines
- Trend analysis

### 2. Documentation
- Keep runbooks updated
- Document incidents
- Maintain troubleshooting guides
- Update procedures
- Share knowledge

### 3. Testing
- Regular testing
- Chaos engineering
- Disaster recovery drills
- Performance testing
- Security testing

## Support Procedures

### 1. Incident Management
1. Issue detection
2. Initial assessment
3. Investigation
4. Resolution
5. Documentation
6. Post-mortem

### 2. Escalation Path
- Level 1: Initial response
- Level 2: Technical analysis
- Level 3: Expert resolution
- Management notification
- Customer communication

## Recovery Procedures

### 1. Backup Recovery
```bash
# Create backup
kubectl exec <pod-name> -- backup.sh

# Restore from backup
kubectl exec <pod-name> -- restore.sh <backup-file>
```

### 2. System Recovery
```bash
# Node recovery
kubectl drain <node-name>
kubectl uncordon <node-name>

# Pod recovery
kubectl delete pod <pod-name>
kubectl rollout restart deployment <deployment-name>
```

## Preventive Measures

### 1. Resource Management
- Set appropriate limits
- Monitor utilization
- Implement autoscaling
- Regular cleanup
- Capacity planning

### 2. Security Measures
- Regular updates
- Security scanning
- Access control
- Audit logging
- Compliance checking

## Documentation and Learning

### 1. Incident Documentation
- Issue description
- Root cause analysis
- Resolution steps
- Prevention measures
- Lessons learned

### 2. Knowledge Sharing
- Team training
- Documentation updates
- Best practices
- Regular reviews
- Feedback loops
