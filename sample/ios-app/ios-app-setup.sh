#!/usr/bin/env bash

set -e

sudo su <<'EOF'
echo "Verifying sshd is enabled..."
sshd_status=$(systemsetup -getremotelogin)
if [[ $string == *": On"* ]]; then
  echo "Ok"
else
  systemsetup -setremotelogin on
  echo "Ok"
fi
EOF

echo ""
echo "Verifying private key is created"

if [[ -f "$HOME/.ssh/marathon" ]]; then
  echo "Ok"
else
  ssh-keygen -m PEM -t rsa -b 4096 -C "$USER@localhost" -f ~/.ssh/marathon -q -N ""
  echo "Ok"
fi

echo ""
echo "Verifying private key is accepted"
status=$(ssh -o BatchMode=yes -o ConnectTimeout=5 "$USER@localhost" echo ok 2>&1) || true
case $status in
ok) echo "Ok" ;;
*)
  ssh-copy-id -i ~/.ssh/marathon "$USER@localhost"
  echo "Ok"
  ;;
esac

echo ""
echo "Select the simulator to use:"
if [[ -f "Marathondevices" ]]; then
  chosen_sim_udid=$(cat Marathondevices | head -2 | tail -1 | egrep -o '[0-9ABCDEF-]+')
  echo "Found Marathondevices file. Assuming that we're still using simulator with UDID=$chosen_sim_udid"
  echo "If that's not the case - remove the Marathondevices file"
  echo "Ok"
else
  simlist=$(xcrun simctl list devices -j | jq -r '.devices | flatten | to_entries | map({name: .value.name, id: .key}) | flatten | map("[\(.id)]: \(.name)") | .[]' | pr -2 -t - | sed '/^$/d')
  echo "$simlist"
  read -p "Enter simulator id:" simid
  chosen_sim_udid=$(xcrun simctl list devices -j | jq -r '.devices | flatten | to_entries | .['"$simid"'] | .value.udid')
  echo "Chosen simulator UDID: $chosen_sim_udid"
  echo "$(pwd)"
  sed 's/XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX/'"$chosen_sim_udid"'/g' Marathondevices.template >Marathondevices

  echo "Ok"
fi

echo ""
echo "Building the app for simulator:"
bash build-for-testing.sh "$chosen_sim_udid"
