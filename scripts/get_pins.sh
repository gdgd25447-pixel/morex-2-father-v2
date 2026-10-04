#!/usr/bin/env bash
# يحسب دبابيس SPKI (sha256/...) لسلسلة شهادات السيرفر لاستخدامها في MOREX_CERT_PINS.
# الاستخدام:  ./scripts/get_pins.sh morex-1-server.onrender.com
set -euo pipefail
HOST="${1:-morex-1-server.onrender.com}"
echo "# شهادات $HOST (الورقة ثم الوسيطة). ثبّت الوسيطة على الأقل: الورقة تتجدد تلقائياً."
openssl s_client -servername "$HOST" -connect "$HOST:443" -showcerts </dev/null 2>/dev/null \
 | awk '/BEGIN CERT/{c++} c{print > "/tmp/morex_cert_" c ".pem"} /END CERT/{}' 
i=0
for f in /tmp/morex_cert_*.pem; do
  i=$((i+1))
  pin=$(openssl x509 -in "$f" -pubkey -noout | openssl pkey -pubin -outform der \
        | openssl dgst -sha256 -binary | openssl enc -base64)
  subj=$(openssl x509 -in "$f" -noout -subject | sed 's/subject=//')
  echo "[$i] sha256/$pin   # $subj"
done
rm -f /tmp/morex_cert_*.pem
echo
echo "ضع اثنين على الأقل (الحالي + احتياطي) في MOREX_CERT_PINS مفصولين بفاصلة."
