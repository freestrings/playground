<?php

function comp($d1, $d2) {
	if(is_array($d1) || is_object($d1)) {
		if((count((array)$d1) != count((array)$d2)) || (gettype($d1) !== gettype($d2))) {
			return false;
		}
		foreach($d1 as $k1=>$v1) {
			if(!comp($v1, is_array($d2) ? $d2[$k1] : (is_object($d2) ? $d2->{$k1} : $d2))) {
				return false;
			}
		}
		return true;
	} else {
		return $d1 === $d2;
	}
}

$v1 = new stdClass;
$v1->a = 1;
$v1->b = new stdClass;
$v1->c = array(new stdClass, 1);

$v2 = new stdClass;
$v2->a = 1;
$v2->b = new stdClass;
$v2->c = array(new stdClass);

$a = array(1, 'a' => 1, 'b' => array(1), $v1, $v2);
$b = array(1, 'a' => 1, 'b' => array(1), $v1, $v1);

$iteration = 100000;
$start = microtime(true);
for($i = 0 ; $i < $iteration; $i++) {
	if(comp($a, $b)) {
		echo 'fail';
	}
}
$end = microtime(true);
echo $end-$start; //약 1.2초 
echo "\n";

