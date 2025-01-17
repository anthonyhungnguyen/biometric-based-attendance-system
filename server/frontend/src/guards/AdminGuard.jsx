import PATH from "constants/path"
import ROLE from "constants/role"
import React from "react"
import { useSelector } from "react-redux"
import { Redirect, Route } from "react-router"

export default function AdminGuard(children) {
  const { component: Component, ...rest } = children
  const { user } = useSelector(state => state.auth)
  return (
    <Route
      {...rest}
      render={props => {
        if (
          !user?.isLoggedIn &&
          !localStorage.getItem("user") &&
          !user?.roles?.includes(ROLE.ADMIN)
        ) {
          return <Redirect to={PATH.STUDENT.HOME} />
        }
        return <Component {...props} />
      }}
    />
  )
}
