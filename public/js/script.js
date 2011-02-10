/* Set serviceMode to true to create your own shapes: */
var serviceMode = false;

$(document).ready(function(){
	/* This code is executed after the DOM has been completely loaded */

	var str=[];
	var perRow = 16;
	
	/* Generating the dot divs: */
	
	for(var i=0;i<192;i++)
	{
		str.push('<div class="dot" id="d-'+i+'" />');
	}
	
	/* Joining the array into a string and adding it to the inner html of the stage div: */
	
	$('#stage').html(str.join(''));
	
	/* Using the hover method: */

	$('#navigation li a').hover(function(e){
	
		/* serviceDraw is a cut-out version of the draw function, used for shape editing and composing: */
		
		if(serviceMode)
			serviceDraw($(this).attr('class'));
		else
			draw($(this).attr('class'));
	}, function(e){
		
	});
	
	/* Caching the dot divs into a variable for performance: */
	dots = $('.dot');
	
	if(serviceMode)
	{
		/* If we are in service mode, show borders around the dot divs, add the export link, and listen for clicks: */
		
		dots.css({
			border:'1px solid black',
			width:dots.eq(0).width()-2,
			height:dots.eq(0).height()-2,
			cursor:'pointer'
		})
		
		$('<div/>').css({
			position:'absolute',
			bottom:-20,
			right:0
		}).html('<a href="" onclick="outputString();return false;">[Export Shape]</a>').appendTo('#stage');
		
		dots.click(function(){
			$(this).toggleClass('active');
		});
	}
	
});

var shapes={
	
	/* Each shape is described by an array of points. You can add your own shapes here,
	   just don't forget to add a coma after each array, except for the last one */
	
	imagefile:[3,4,5,6,7,8,9,10,19,26,27,35,39,42,43,44,51,54,55,56,60,67,69,70,71,72,73,76,83,92,99,104,105,106,108,115,119,120,121,122,124,131,135,136,137,138,140,147,151,152,153,156,163,172,179,180,181,182,183,184,185,186,187,188],
	bomb:[7,8,13,15,22,25,30,38,42,45,47,52,53,54,55,56,59,60,68,69,70,71,72,83,84,85,86,87,88,89,98,99,101,102,103,104,105,106,114,116,117,118,119,120,121,122,130,133,134,135,136,137,138,146,147,150,151,152,153,154,163,164,166,167,168,169,180,181,182,183,184],
	wut:[0,1,2,3,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,21,22,23,24,25,26,27,28,29,30,31,32,34,35,37,38,39,40,45,46,47,48,50,51,53,54,55,57,58,59,60,62,63,64,65,66,67,69,70,71,73,76,78,79,80,81,82,83,85,86,87,92,94,95,96,97,98,99,101,102,103,106,107,108,110,111,112,113,117,118,119,122,126,127,128,129,130,132,133,142,143,144,145,146,148,150,151,154,158,159,160,164,165,166,167,168,173,174,175,176,177,178,180,181,182,183,184,185,186,187,188,189,190,191],
	padlock:[7,8,9,10,11,12,22,23,24,25,26,27,28,29,30,38,39,45,46,54,55,61,62,70,71,77,78,80,81,82,83,84,85,86,87,88,94,96,97,98,99,100,101,102,103,104,109,110,112,120,128,129,130,131,132,133,134,135,136,144,152,160,161,162,163,164,165,166,167,168,177,178,179,180,181,182,183]
}

var stopCounter = 0;
var dots;

function draw(shape)
{
	/* This function draws a shape from the shapes object */
	
	stopCounter++;
	var currentCounter = stopCounter;

	dots.removeClass('active').css('opacity',0);
	
	$.each(shapes[shape],function(i,j){
		setTimeout(function(){
							
			/* If a different shape animaton has been started during the showing of the current one, exit the function  */
			if(currentCounter!=stopCounter) return false;
			
			dots.eq(j).addClass('active').fadeTo(100,0.6);
			
			/* The fade animation is scheduled for 10*i millisecond in the future: */
		},4*i);

	});
}

function serviceDraw(shape)
{
	/* A cut out version of the draw function, used in service mode */
	
	dots.removeClass('active');
	
	$.each(shapes[shape],function(i,j){
		dots.eq(j).addClass('active');
	});
}

function outputString()
{
	/* Outputs the positions of the active dot divs as a comma-separated string: */
	
	var str=[];
	$('.dot.active').each(function(){
		
		str.push(this.id.replace('d-',''));
	})
	
	prompt('Insert this string as an array in the shapes object',str.join(','));
}
