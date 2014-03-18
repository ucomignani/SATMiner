using namespace Minisat;

//=================================================================================================
// Model manager

bool Solver::counterModel(){
  
  Lit q;
  vec<Lit> ms;  

  vec<Lit> learnt_model, real_model;
  
  for(int i = 0; i < trail.size(); i++){
    q = trail[i];
  
    if(level(var(q)) > 0 && isBackDoorVar(q) && !sign(q)) 
	learnt_model.push(~q);
	

  if(isBackDoorVar(q) && !sign(q)) 
	 real_model.push(q);	
  }
  
  nbmodels++;
  printModel(real_model);
  if (limitmodels != -1 && nbmodels >= limitmodels) {
    return false;
  }

  cancelUntil(0);
  
  if(learnt_model.size() == 0) return false;
  else if(learnt_model.size() == 1)
    uncheckedEnqueue(learnt_model[0]);
  else{
    CRef cr = ca.alloc(learnt_model, true);
    models.push(cr);
    attachClause(cr);
  }
  return true;
}

//=================================================================================================

/* void Solver::removeLastJockerLits(vec<Lit>& lits){ */
/*  int i = lits.size()-1; */
/*   while(isJockerVar(lits[i])){ */
/*    lits.pop(); */
/*    i = lits.size()-1; */
/*   } */
/* }  */
  

/* //================================================================================================= */
/* void Solver::removeJockerLits(vec<Lit>& lt){ */
/*  int i, j ; */
/*  for(i = j = 0; i < lt.size(); i++) */
/*     if(!isJockerVar(lt[i])) */
/* 	lt[j++] = lt[i]; */
/*  lt.shrink(i-j); */
/* return; */

/*   i = 0, j = lt.size(); */
/*   while(i < j){ */
/*     if(isJockerVar(lt[i])){ */
/* 	lt[i] = lt[--j]; */
/* 	lt.pop(); */
/*     }else */
/*       i++; */
/*   } */
/* } */

//=================================================================================================
//Debug
void Solver::printModel(vec<Lit>& lits){
sort(lits);
  for(int i = 0; i < lits.size(); i++)
    printf("%s%d ", sign(lits[i]) ? "-" : "", var(lits[i])+1);
  printf("\n");
}

//=================================================================================================

