// =====================================================================
//  Test de charge k6 — génère du trafic pour déclencher l'autoscaling (HPA)
//  du covoiturageService. On tape sur l'app via l'ingress :
//    nginx (frontend) -> api-gateway -> covoiturageService -> MySQL
//  Le CPU du covoiturageService grimpe -> l'HPA ajoute des pods.
// =====================================================================
import http from 'k6/http';
import { check, sleep } from 'k6';

// URL de base surchargée par la variable d'env BASE_URL (sinon l'IP du nœud master)
const BASE_URL = __ENV.BASE_URL || 'http://192.168.5.154';

export const options = {
  stages: [
    { duration: '30s', target: 60 },  // montée progressive à 60 utilisateurs virtuels
    { duration: '90s', target: 60 },  // charge soutenue -> CPU monte -> HPA scale UP
    { duration: '30s', target: 0 },   // arrêt -> CPU retombe -> HPA scale DOWN ensuite
  ],
};

export default function () {
  const res = http.get(`${BASE_URL}/api/covoiturage/all`);
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(0.3);
}
