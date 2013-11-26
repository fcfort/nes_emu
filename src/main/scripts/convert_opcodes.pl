#!perl

use warnings;
use strict;

my %enum_name = (
    "Implied" => "IMPLICIT",
    "Accumulator" => "ACCUMULATOR",
    "Immediate" => "IMMEDIATE",
    "Zero Page" => "ZERO_PAGE",
    "Zero Page,X" => "ZERO_PAGE_X",
    "Zero Page,Y" => "ZERO_PAGE_Y",
    "Relative" => "RELATIVE",
    "Absolute" => "ABSOLUTE",
    "Absolute,X" => "ABSOLUTE_X",
    "Absolute,Y" => "ABSOLUTE_Y",
    "Indirect" => "INDIRECT",
    "(Indirect,X)" => "INDIRECT_X",
    "(Indirect),Y" => "INDIRECT_Y",
);

my %enum_code = (
    "Implied" => "",
    "Accumulator" => "ac",
    "Immediate" => "i",
    "Zero Page" => "z",
    "Zero Page,X" => "zx",
    "Zero Page,Y" => "zy",
    "Relative" => "",
    "Absolute" => "a",
    "Absolute,X" => "ax",
    "Absolute,Y" => "ay",
    "Indirect" => "",
    "(Indirect,X)" => "ix",
    "(Indirect),Y" => "iy",
);


while(<>) {
    my ($id, $printname, $cycles, $length, $addressing);
    my ($branch, $page) = (0,0);
    if(m/id="(..)" printName="(...)" cycles="(\d+)" length="(\d+)" addressingMode="(.*?)"/) {
       $id = $1;
       $printname = $2;
       $cycles = $3;
       $length = $4;
       $addressing = $5;
       if($addressing eq '') {
            print "Failed to match addressing mode on $_\n";
       }

       if(/onBranchCycle/) {
            $branch = 1;
       }
       if(/onPageJumpCycle/) {
            $page = 1;
       }

        if(not $branch and not $page) {
            print $printname.$enum_code{$addressing} . qq/("$id", "$printname", $cycles, $length, AddressingMode.$enum_name{$addressing}),/ . "\n";
        } else {
            print $printname.$enum_code{$addressing} . qq/("$id", "$printname", $cycles, $length, / . ($branch ? "true" : "false") .", " . ($page ? "true" : "false") . ", "  . qq/AddressingMode.$enum_name{$addressing}),/ . "\n";
        }
    } else {
        print "failed to convert $_\n";
    }

}
