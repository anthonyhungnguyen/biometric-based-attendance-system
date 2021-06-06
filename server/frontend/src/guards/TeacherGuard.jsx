import PATH from "constants/path"
import ROLE from "constants/role"
import React from "react"
import { useSelector } from "react-redux"
import { Redirect, Route } from "react-router"

export default function TeacherGuard(children) {
  const { component: Component, ...rest } = children
  const { user } = useSelector(state => state.auth)
  return (
    <Route
      {...rest}
      render={props => {
        if (
          !user?.isLoggedIn &&
          !localStorage.getItem("user") &&
          !user?.roles.includes(ROLE.TEACHER)
        ) {
          return <Redirect to={PATH.COMMON.LOGIN} />
        }
        return <Component {...props} />
      }}
    />
  )
}
