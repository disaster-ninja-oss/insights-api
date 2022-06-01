CHART_PATH='./external/helm/insights-api/'
KUBECFG_FILE="./external/kubeconfig.yaml"
#STAGE is passed from CI job
RELEASE=$STAGE-insights-api
USER=$STAGE-insights-api@kontur.io
VALUES="$CHART_PATH/values/values-$STAGE.yaml"
KUBECTL_OPTS="--kubeconfig $KUBECFG_FILE --context $STAGE"
HELM_OPTS="--kubeconfig $KUBECFG_FILE --kube-context $STAGE"

#Set secrets from Gitlab environment variables to bot user
echo "kubectl $KUBECTL_OPTS config set clusters.$STAGE.certificate-authority-data (secret data)"
kubectl $KUBECTL_OPTS config set clusters.$STAGE.certificate-authority-data $CERT_AUTH
echo "kubectl $KUBECTL_OPTS config set users.$USER.client-key-data (secret data)"
kubectl $KUBECTL_OPTS config set users.$USER.client-key-data $CLIENT_KEY
echo "kubectl $KUBECTL_OPTS config set users.$USER.client-certificate-data (secret data)"
kubectl $KUBECTL_OPTS config set users.$USER.client-certificate-data $CLIENT_CERT

#render templates to save as Gitlab job artifacts
echo "helm $HELM_OPTS template $RELEASE $CHART_PATH -f $VALUES > pre-manifests.yaml"
helm $HELM_OPTS template $RELEASE $CHART_PATH -f $VALUES > pre-manifests.yaml

#install or upgrade the release
echo "helm $HELM_OPTS upgrade --install $RELEASE $CHART_PATH -f $VALUES -o yaml > manifests.yaml"
helm $HELM_OPTS upgrade --install $RELEASE $CHART_PATH -f $VALUES -o yaml > manifests.yaml