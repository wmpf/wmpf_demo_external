#!/bin/sh

time_stamp=`date +%s`

function CheckStop()
{
	if [ $? -ne 0 ]; then
		echo "execute fail, error on line_no:"$1"  exit!!!"
		exit
	fi
}

function GenEcdsaKey()
{
	ec_param_file_path="/tmp/ec_param.pem."$time_stamp
	openssl ecparam -out $ec_param_file_path -name prime256v1 -genkey	
	CheckStop $LINENO
	openssl genpkey -paramfile $ec_param_file_path -out $1 
	CheckStop $LINENO
	openssl pkey  -in $1 -inform PEM  -out $2 -outform PEM     -pubout
	CheckStop $LINENO
	rm $ec_param_file_path
	echo "gen_ecdsa_key succ prikey_path:"$1" pubkey_path:"$2
}

function GenEcdsaSign()
{
	ec_sign_info_file="/tmp/ec_sign_info_file."$time_stamp
	ec_sign_info_sha256="/tmp/ec_sign_info_sha256."$time_stamp
	ec_binary_sign_file="/tmp/ec_binary_sign_file."$time_stamp
	echo -n "$1"_"$2" > $ec_sign_info_file 
	openssl dgst -sha256 -binary -out $ec_sign_info_sha256 $ec_sign_info_file 
	CheckStop $LINENO
	openssl pkeyutl -sign -in $ec_sign_info_sha256   -out $ec_binary_sign_file  -inkey $3 -keyform PEM
	CheckStop $LINENO
	openssl base64 -e -in $ec_binary_sign_file -out $4
	CheckStop $LINENO
	rm $ec_sign_info_file $ec_sign_info_sha256 $ec_binary_sign_file
	echo "gen_ecdsa_sign succ sign_file_path:"$4
}

function VerifyEcdsaSign()
{
	ec_sign_info_file="/tmp/ec_sign_info_file."$time_stamp
	ec_sign_info_sha256="/tmp/ec_sign_info_sha256."$time_stamp
	ec_binary_sign_file="/tmp/ec_binary_sign_file."$time_stamp
	echo -n "$1"_"$2" > $ec_sign_info_file 
	openssl dgst -sha256 -binary -out $ec_sign_info_sha256 $ec_sign_info_file 
	CheckStop $LINENO
	openssl base64 -d -in $4 -out $ec_binary_sign_file	
	CheckStop $LINENO
	openssl pkeyutl -verify -in $ec_sign_info_sha256  -sigfile $ec_binary_sign_file   -pubin -inkey $3 -keyform PEM 
	rm $ec_sign_info_file $ec_sign_info_sha256 $ec_binary_sign_file
}

function Usage()
{
	echo "Usage:"
	echo "mmiot_ecdsa_sign.sh gen_ecdsa_key <private_key_file_path> <public_key_file_path>"
	echo "mmiot_ecdsa_sign.sh gen_ecdsa_sign <product_id> <sn> <private_key_file_path> <signature_file_path> "
	echo "mmiot_ecdsa_sign.sh verify_ecdsa_sign <product_id> <sn> <public_key_file_path> <signature_file_path> "
}



if [[ $# -eq 3 && $1 = "gen_ecdsa_key" ]];then
	GenEcdsaKey $2 $3

elif [[ $# -eq 5 && $1 = "gen_ecdsa_sign" ]];then
	GenEcdsaSign $2 $3 $4 $5

elif [[ $# -eq 5 && $1 = "verify_ecdsa_sign" ]];then
	VerifyEcdsaSign $2 $3 $4 $5
else
	echo "------------------- Invalid Args !!! -------------------"
	Usage
fi


