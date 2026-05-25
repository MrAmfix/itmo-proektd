from typing import Optional

from pydantic import BaseModel

from app.models.user import FitnessGoal, FitnessLevel


class ProfileResponse(BaseModel):
    gender: Optional[str] = None
    age: Optional[int] = None
    weight: Optional[float] = None
    height: Optional[float] = None
    goal: Optional[FitnessGoal] = None
    level: Optional[FitnessLevel] = None
    injuries: Optional[list[str]] = None
    equipment: Optional[list[str]] = None
    onboarding_completed: bool = False
    medical_disclaimer_accepted: bool = False

    class Config:
        from_attributes = True


class ProfileUpdate(BaseModel):
    gender: Optional[str] = None
    age: Optional[int] = None
    weight: Optional[float] = None
    height: Optional[float] = None
    goal: Optional[FitnessGoal] = None
    level: Optional[FitnessLevel] = None
    injuries: Optional[list[str]] = None
    equipment: Optional[list[str]] = None
