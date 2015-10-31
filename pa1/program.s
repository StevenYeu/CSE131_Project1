
	.section	".text",#alloc,#execinstr

.L_y0:
	.align	8
	.word	65536
	.word	65536
	.word	65536
	.word	65536

	! block 0

	.global	main
	.type	main,#function
main:
.L_y3:
	save	%sp,-104,%sp

	! block 1
.L13:

! File program.c:
!    1	int main(){
!    2	    int x = 0;

	st	%g0,[%fp-8]

!    3	    return x;

	st	%g0,[%fp-4]
	mov	0,%i0
	jmp	%i7+8
	restore

	! block 2
.L12:
	mov	0,%i0
	jmp	%i7+8
	restore
	.size	main,(.-main)
.L_y4:
	.align	8

	.section ".annotate",#progbits

	! ANNOTATION: Header
	.asciz "anotate"
	.half 6
	.half 0
	.word .L_y2 - .L_y1
.L_y1:
	! ANNOTATION: Module
	.half 0
	.half .L_y6 - .L_y5
.L_y5:
	.word .L_y0
	.word .L_y4 - .L_y0
.L_y6:
	! ANNOTATION: Function
	.half 1
	.half .L_y8 - .L_y7
.L_y7:
	.word .L_y3
	.word .L_y4 - .L_y3
	.word 5767296
.L_y8:
.L_y2:
	! ANNOTATION: End

	.section	".bss",#alloc,#write
Bbss.bss:
	.skip	0
	.type	Bbss.bss,#object
	.size	Bbss.bss,0

	.section	".data",#alloc,#write
Ddata.data:
	.skip	0
	.type	Ddata.data,#object
	.size	Ddata.data,0

	.section	".rodata",#alloc
Drodata.rodata:
	.skip	0
	.type	Drodata.rodata,#object
	.size	Drodata.rodata,0

	.file	"program.c"
	.ident	"acomp: Sun C 5.12 SunOS_sparc Patch 148917-07 2013/10/18"

	.global	__fsr_init_value
__fsr_init_value = 0x0
!  Begin sdCreateSection : .debug_info
!  Section Info: link_name/strtab=, entsize=0x1, adralign=0x1, flags=0x0
!  Section Data Blocks:
!   reloc[0]: knd=2, off=6, siz=4, lab1=.debug_abbrev, lab2=, loff=0
!   reloc[1]: knd=2, off=202, siz=4, lab1=.debug_line, lab2=, loff=0
	.section ".debug_info"
	.byte 0x00,0x00,0x00,0xcb,0x00,0x02
	.uaword %section_symbol(".debug_abbrev")
	.byte 0x04,0x01
	.ascii "program.c\0"
	.byte 0x0c
	.ascii "/home/solaris/ieng9/cs131f/shl166/pa1\0"
	.ascii " /software/common/solstudio12/prod/bin/cc -S  program.c\0"
	.ascii "Xa;R=Sun C 5.12 SunOS_sparc Patch 148917-07 2013/10/18;backend;raw;cd;\0"
	.ascii "DBG_GEN 5.3.3\0"
	.uaword %section_symbol(".debug_line")
	.byte 0x00
!  End sdCreateSection
!  Begin sdCreateSection : .debug_line
!  Section Info: link_name/strtab=, entsize=0x1, adralign=0x1, flags=0x0
!  Section Data Blocks:
	.section ".debug_line"
	.byte 0x00,0x00,0x00,0x23,0x00,0x02,0x00,0x00
	.byte 0x00,0x1d,0x04,0x00,0xff,0x04,0x0a,0x00
	.byte 0x01,0x01,0x01,0x01,0x00,0x00,0x00,0x01
	.byte 0x00,0x70,0x72,0x6f,0x67,0x72,0x61,0x6d
	.byte 0x2e,0x63,0x00,0x00,0x00,0x00,0x00
!  End sdCreateSection
!  Begin sdCreateSection : .debug_abbrev
!  Section Info: link_name/strtab=, entsize=0x1, adralign=0x1, flags=0x0
!  Section Data Blocks:
	.section ".debug_abbrev"
	.byte 0x01,0x11,0x00,0x03,0x08,0x13,0x0b,0x1b
	.byte 0x08,0x85,0x44,0x08,0x87,0x44,0x08,0x25
	.byte 0x08,0x10,0x06,0x00,0x00,0x00
!  End sdCreateSection
