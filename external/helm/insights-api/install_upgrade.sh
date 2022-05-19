CHART_PATH='./external/helm/insights-api/'
KUBECFG_FILE="./external/kubeconfig.yaml"
STAGE=$1
RELEASE=insights-api-$STAGE
VALUES="$CHART_PATH/values/values-$STAGE.yaml"
KUBECTL_OPTS="--kubeconfig $KUBECFG_FILE --context $STAGE"
HELM_OPTS="--kubeconfig $KUBECFG_FILE --kube-context $STAGE"

#Set secrets from Gitlab environment variables #TODO use gitlab bot
kubectl $KUBECTL_OPTS config set clusters.$STAGE.certificate-authority-data $CERT_AUTH
kubectl $KUBECTL_OPTS config set users.isemichastnov@kontur.io.client-key-data $CLIENT_KEY
kubectl $KUBECTL_OPTS config set users.isemichastnov@kontur.io.client-certificate-data $CLIENT_CERT

#render templates to save as Gitlab job artifacts
helm $HELM_OPTS template $RELEASE $CHART_PATH -f $VALUES > manifests.yaml

#Check if specified release exists
helm $HELM_OPTS upgrade --install $RELEASE $CHART_PATH -f $VALUES