import{a as P}from"./chunk-U6NWN7QW.js";import{$a as C,G as u,I as m,J as d,Q as g,S as v,W as h,X as i,Xa as w,Y as r,Z as _,ba as x,da as p,fa as l,ma as f,oa as b,u as s,v as c}from"./chunk-6GTILWTJ.js";function y(o,t){if(o&1){let e=x();i(0,"app-remove-confirmation-modal",6),p("confirm",function(){s(e);let a=l();return c(a.confirmLogout())})("cancel",function(){s(e);let a=l();return c(a.cancelLogout())}),r()}if(o&2){let e=l();h("title","Are you sure you want to log out?")("contentMessage","You will be signed out of your account and will need to log in again to continue.")("buttonContent","Yes, Logout")("svg",e.icon)}}var k=class o{constructor(t,e){this.authService=t;this.router=e}username="";errorMessage="";showPanel=!1;icon=`<svg
				xmlns="http://www.w3.org/2000/svg"
				fill="none"
				viewBox="0 0 24 24"
				stroke-width="2"
				stroke="currentColor"
				class="w-12 h-12 mx-auto text-red-500 dark:text-red-400"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h6a2 2 0 012 2v1"
				/>
			</svg>`;ngOnInit(){this.authService.getActiveUser().subscribe({next:t=>this.username=t.username,error:t=>this.errorMessage="Unexpected error while retrieving the user"})}confirmLogout(){this.authService.logoutUser().subscribe(t=>this.router.navigate(["/"]))}cancelLogout(){this.showPanel=!1}logoutButton(){this.showPanel=!0}static \u0275fac=function(e){return new(e||o)(m(C),m(w))};static \u0275cmp=d({type:o,selectors:[["app-profile"]],decls:8,vars:2,consts:[[1,"min-h-screen","app-background","flex","flex-col","items-center","justify-center","gap-8","p-6"],[1,"text-3xl","font-semibold","text-blue-600","dark:text-blue-400"],[1,"relative","inline-flex","items-center","justify-center","px-6","py-3","overflow-hidden","font-medium","text-white","cursor-pointer","transition-all","duration-300","bg-gradient-to-r","from-red-500","to-rose-600","rounded-xl","shadow-lg","group","hover:from-red-600","hover:to-rose-700","hover:scale-105",3,"click"],[1,"absolute","inset-0","transition-transform","duration-300","ease-out","translate-x-full","bg-white","opacity-10","group-hover:translate-x-0"],[1,"relative","z-10","text-lg","tracking-wide"],[3,"title","contentMessage","buttonContent","svg"],[3,"confirm","cancel","title","contentMessage","buttonContent","svg"]],template:function(e,n){e&1&&(i(0,"div",0)(1,"h1",1),f(2),r(),i(3,"button",2),p("click",function(){return n.logoutButton()}),_(4,"span",3),i(5,"span",4),f(6,"Logout"),r()(),g(7,y,1,4,"app-remove-confirmation-modal",5),r()),e&2&&(u(2),b(" ",n.username," "),u(5),v(n.showPanel?7:-1))},dependencies:[P],encapsulation:2})};export{k as ProfileComponent};
